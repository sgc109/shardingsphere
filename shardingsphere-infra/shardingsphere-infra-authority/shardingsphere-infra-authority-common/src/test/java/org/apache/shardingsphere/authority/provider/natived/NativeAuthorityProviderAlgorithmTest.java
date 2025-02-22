/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.authority.provider.natived;

import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.AuthorityProvideAlgorithm;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class NativeAuthorityProviderAlgorithmTest {
    
    @BeforeClass
    public static void setUp() {
        ShardingSphereServiceLoader.register(AuthorityProvideAlgorithm.class);
    }
    
    @Test
    public void testAlgorithmType() {
        NativeAuthorityProviderAlgorithm algorithm = (NativeAuthorityProviderAlgorithm) TypedSPIRegistry.findRegisteredService(AuthorityProvideAlgorithm.class, "NATIVE", new Properties()).get();
        assertThat(algorithm.getType(), is("NATIVE"));
    }
    
    @Test
    public void testFindPrivileges() throws SQLException {
        NativeAuthorityProviderAlgorithm algorithm = new NativeAuthorityProviderAlgorithm();
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        ShardingSphereMetaData metaData = mockShardingSphereMetaData(users);
        algorithm.init(Collections.singletonMap("db0", metaData), users);
        Optional<ShardingSpherePrivileges> privileges = algorithm.findPrivileges(new Grantee("root", "localhost"));
        assertTrue(privileges.isPresent());
        assertPrivilege(privileges.get());
    }
    
    @Test
    public void testRefreshPrivileges() throws SQLException {
        NativeAuthorityProviderAlgorithm algorithm = new NativeAuthorityProviderAlgorithm();
        Collection<ShardingSphereUser> users = Collections.singletonList(new ShardingSphereUser("root", "", "localhost"));
        algorithm.init(Collections.emptyMap(), users);
        Optional<ShardingSpherePrivileges> privileges1 = algorithm.findPrivileges(new Grantee("root", "localhost"));
        assertTrue(privileges1.isPresent());
        assertThat(privileges1.get().hasPrivileges(Collections.singletonList(PrivilegeType.SUPER)), is(true));
        algorithm.refresh(Collections.singletonMap("db0", mockShardingSphereMetaData(users)), users);
        Optional<ShardingSpherePrivileges> privileges2 = algorithm.findPrivileges(new Grantee("root", "localhost"));
        assertTrue(privileges2.isPresent());
        assertPrivilege(privileges2.get());
    }
    
    private void assertPrivilege(final ShardingSpherePrivileges privileges) {
        Collection<PrivilegeType> expected = new LinkedList<>();
        expected.add(PrivilegeType.SUPER);
        expected.add(PrivilegeType.SELECT);
        expected.add(PrivilegeType.INSERT);
        expected.add(PrivilegeType.UPDATE);
        expected.add(PrivilegeType.RELOAD);
        expected.add(PrivilegeType.SHUTDOWN);
        assertThat(privileges.hasPrivileges(expected), is(true));
    }
    
    private ShardingSphereMetaData mockShardingSphereMetaData(final Collection<ShardingSphereUser> users) throws SQLException {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        DataSource dataSource = mockDataSourceForPrivileges(users);
        when(result.getResource().getAllInstanceDataSources()).thenReturn(Collections.singletonList(dataSource));
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private DataSource mockDataSourceForPrivileges(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet globalPrivilegeResultSet = mockGlobalPrivilegeResultSet();
        ResultSet schemaPrivilegeResultSet = mockSchemaPrivilegeResultSet();
        ResultSet tablePrivilegeResultSet = mockTablePrivilegeResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        String globalPrivilegeSQL = "SELECT * FROM mysql.user WHERE (user, host) in (%s)";
        String schemaPrivilegeSQL = "SELECT * FROM mysql.db WHERE (user, host) in (%s)";
        String tablePrivilegeSQL = "SELECT Db, Table_name, Table_priv FROM mysql.tables_priv WHERE (user, host) in (%s)";
        String useHostTuples = users.stream().map(item -> String.format("('%s', '%s')", item.getGrantee().getUsername(), item.getGrantee().getHostname())).collect(Collectors.joining(", "));
        when(result.getConnection().createStatement().executeQuery(String.format(globalPrivilegeSQL, useHostTuples))).thenReturn(globalPrivilegeResultSet);
        when(result.getConnection().createStatement().executeQuery(String.format(schemaPrivilegeSQL, useHostTuples))).thenReturn(schemaPrivilegeResultSet);
        when(result.getConnection().createStatement().executeQuery(String.format(tablePrivilegeSQL, useHostTuples))).thenReturn(tablePrivilegeResultSet);
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/test");
        return result;
    }
    
    private ResultSet mockGlobalPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false, true, true, false);
        when(result.getBoolean("Super_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Reload_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Shutdown_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Process_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("File_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Show_db_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Repl_slave_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Repl_client_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Create_user_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Create_tablespace_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Select_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Insert_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Update_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Delete_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Create_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Alter_priv")).thenReturn(true, false, true, false);
        when(result.getBoolean("Drop_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Grant_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Index_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("References_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Create_tmp_table_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Lock_tables_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Execute_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Create_view_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Show_view_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Create_routine_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Alter_routine_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Event_priv")).thenReturn(false, false, false, false);
        when(result.getBoolean("Trigger_priv")).thenReturn(false, false, false, false);
        when(result.getString("user")).thenReturn("root", "mysql.sys", "root", "mysql.sys");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
    
    private ResultSet mockSchemaPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("Db")).thenReturn("sys");
        when(result.getBoolean("Select_priv")).thenReturn(false);
        when(result.getBoolean("Insert_priv")).thenReturn(false);
        when(result.getBoolean("Update_priv")).thenReturn(false);
        when(result.getBoolean("Delete_priv")).thenReturn(false);
        when(result.getBoolean("Create_priv")).thenReturn(false);
        when(result.getBoolean("Alter_priv")).thenReturn(false);
        when(result.getBoolean("Drop_priv")).thenReturn(false);
        when(result.getBoolean("Grant_priv")).thenReturn(false);
        when(result.getBoolean("Index_priv")).thenReturn(false);
        when(result.getBoolean("References_priv")).thenReturn(false);
        when(result.getBoolean("Create_tmp_table_priv")).thenReturn(false);
        when(result.getBoolean("Lock_tables_priv")).thenReturn(false);
        when(result.getBoolean("Execute_priv")).thenReturn(false);
        when(result.getBoolean("Create_view_priv")).thenReturn(false);
        when(result.getBoolean("Show_view_priv")).thenReturn(false);
        when(result.getBoolean("Create_routine_priv")).thenReturn(false);
        when(result.getBoolean("Alter_routine_priv")).thenReturn(false);
        when(result.getBoolean("Event_priv")).thenReturn(false);
        when(result.getBoolean("Trigger_priv")).thenReturn(true);
        when(result.getString("user")).thenReturn("mysql.sys");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
    
    private ResultSet mockTablePrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, false);
        when(result.getString("Db")).thenReturn("sys");
        when(result.getString("Table_name")).thenReturn("sys_config");
        when(result.getArray("Table_priv").getArray()).thenReturn(new String[]{"Select"});
        when(result.getString("user")).thenReturn("mysql.sys");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
}
