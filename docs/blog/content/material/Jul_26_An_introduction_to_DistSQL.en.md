+++

title = "An Introduction to DistSQL"
weight = 16
chapter = true

+++

We believe that if you’re reading this then you’re familiar with SQL (Structured Query Language), the data query and programming language. It’s also used as the standard language of relational database management systems for accessing data, querying, updating and managing relational database systems. 

Similar to standard SQL, DistSQL, or Distributed SQL, is a built-in SQL language unique to ShardingSphere that provides incremental functional capabilities beyond standard SQL. Leveraging ShardingSphere's powerful SQL parsing engine, DistSQL provides a syntax structure and syntax validation system like that of standard SQL - making DistSQL more flexible while maintaining regularity.

ShardingSphere's Database Plus concept aims at creating an Open-Source distributed database system that is both functional and relevant to the actual database business. DistSQL is built on top of the traditional database to provide SQL capabilities that are both standards-compliant and feature ShardingSphere's functionality to better energize the traditional database.

## Original Design Intention of DistSQL

Over its rapid development years, ShardingSphere has become unique in the database middleware space as the kernel has gradually stabilized, and the core functionality has continuously been honed. As an Open-Source leader in Asia and China in particular, ShardingSphere did not stop in its exploration of a distributed database ecosystem. 

Redefining the boundary between middleware and database and allowing developers to leverage Apache ShardingSphere as if they were using a database natively is DistSQL's design goal. It is also an integral part of ShardingSphere's ability to transform from a developer-oriented framework and middleware to an operations-oriented infrastructure product.

## DistSQL Syntax System

DistSQL has been designed from the outset to be standards-oriented, considering the habits of both database developers and operators. The syntax of DistSQL is based on the standard SQL language, considering readability and ease of use, while retaining the maximum amount of ShardingSphere's own features and providing the highest possible number of customization options for users to cope with different business scenarios. Developers familiar with SQL and ShardingSphere can get started quickly.

While standard SQL provides different types of syntaxes such as DQL, DDL, DML, DCL etc. to define different functional SQL statements, DistSQL defines a syntax system of its own as well.

In ShardingSphere, the DistSQL syntax is currently divided into three main types: RDL, RQL and RAL.

RDL (Resource & Rule Definition Language): Resource rule definition language for creating, modifying and deleting resources and rules.

RQL (Resource & Rule Query Language): resource rule query language for querying and presenting resources and rules.

RAL (Resource & Rule Administrate Language): resource rule administration language for incremental functional operations such as Hint, transaction type switching, and query of sharding execution plan.

DistSQL's syntax builds a bridge for ShardingSphere to move towards a distributed database, and while it is still being improved as more ideas are implemented, DistSQL is bound to get more powerful. Developers who are interested are welcome to join ShardingSphere and contribute ideas and code to DistSQL.

For more detailed syntax rules, please refer to the official documentation: [https://[shardingsphere.apache.org/document/current/cn/features/dist-sql/syntax/]()]()

For the project’s community, please refer to the official Slack channel: 
[https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg]()

## DistSQL in Practice

Having understood the design concept and syntax system of DistSQL, let’s take data sharding as an example to demonstrate how to build a data sharding service based on ShardingSphere.

### Environment preparation:
1. Start MySQL Services
2. Create a MySQL database for sharding
3. Start the Zookeeper service
4. Turn on the distributed governance configuration and start ShardingSphere-Proxy [(https://shardingsphere.apache.org/document/current/cn/quick-start/shardingsphere-proxy-quick-start/)]()

### Practical demonstration:
* Connect to the launched ShardingSphere-Proxy using the MySQL command line
* Create and query the distributed database `sharding_db`

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1k5QxxXaiamXuXXjyoXLCURXB2mWuiaFTqicurdVqsEmv941e1QX77ibseg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

* Use the newly created database

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1kOw8DibEWlUWExm51MyicHe2MibZ7NflDTpiceQbE76E17E6HqDOXFzqGQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

* Execute RDL to configure 2 data source resources `ds_1` and `ds_2` for sharding

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1VEGFny6NTTFvJwupZgScic32CWU5R7FSYcJ2Xxa9DQL0QGbkkenHkrw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

* Execute RQL to query the newly added data source resources

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1aFLiaEibVjTsp7sRNAtt1iafiaLno2NCgPIvK0wQUjrJ2ncG6sHKib94fjw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

* Execute RDL to create a sharding rule for the `t_order` table

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb10UicJL0Y31TGOPHYPn9OicAyGdEODsU1NCFic2EOJJ4nDZ8uvBpia7mUEw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

* Execute RQL to query the sharding rules

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1XuOarBG79VdVsRGz5BicvD6CgnzxCzGR7UjkkcG3yKbqTRnjYGO8CCQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

In addition to querying all sharding rules under the current database, RQL can also query individual tables for sharding rules with the following statement
`SHOW SHARDING TABLE RULE t_order FROM sharding_db`

* Creating and querying the `t_order` sharding table

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1RcBmlArC4e8CdpD9WTAePONjibUu4RGapEeDLP4LDwTyIHBLOTZEAgg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

* After successfully creating the sharding table `t_order` on the ShardingSphere-Proxy side, ShardingSphere automatically creates the sharding table based on the sharding rules of the `t_order` table by connecting to the underlying databases `ds_1` and `ds_2` via the client side.

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb17UGyVbiaeHRZFf7njF2WqMPjjkBsXwToxJOmCCftqeBaSpEwv0W3djQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1cWejp3FF3RM6Q5hEjzoHQPb8cPErqxM7V0qWjRVOl6Ag4oYsc1JtBg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

* Once the sharding table is created, continue to execute the SQL statement on the ShardingSphere-Proxy side to insert the data

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1LGUpTbl43ScONHXic9Z4Vc1fzzjufeI9x7iaoDjicbOwty9PjHPnmDFfQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

* Query the execution plan via RAL

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1Ruib2QEMSkuAtDOPGpdMkAHicetjEEYaTriaW3b4nl5s2KwueXvKJJX8w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

This completes the ShardingSphere data sharding service using DistSQL. Compared to the previous version of the ShardingSphere proxy, which was profile-driven, DistSQL is more developer-friendly and more flexible in managing resources and rules. Moreover, the SQL-driven approach enables seamless interfacing between DistSQL and standard SQL.

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1jmhjJrAEvpyYS8q0KZgicB1bJJByDBFKdibCcuL25Qk0AEosibTqd1f4Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

In the above example, only a small part of the DistSQL syntax is demonstrated. In addition to creating and querying resources and rules via `CREATE` and `SHOW` statements, DistSQL also provides additional operations such as `ALTRE DROP`, and supports configuration control of data sharding’s core functions, read and write separation, data encryption and database discovery. 

## Conclusion

As one of the new features released in Apache ShardingSphere’s 5.0.0-beta, DistSQL will continue to build on this release to provide improve syntax and increasingly powerful functions.

DistSQL has opened up endless possibilities for ShardingSphere to explore in the distributed database space, and in the future DistSQL will be used as a link to connect more functions and provide one-click operations. For example, we will analyze the overall database status with one click, connect with elastic migration, provide one-click data expansion and shrinkage, and connect with control to realize one-click master-slave switch and change database status.

We warmly welcome Open-Source and Java script enthusiasts to join our Slack community or check our GitHub to learn more about ShardingSphere’s latest developments. 

### Author

**Meng Haoran**

SphereEx Senior Java Engineer

Apache ShardingSphere Committer

Previously responsible for the database products R&D at JingDong Technology, he is about Open-Source passionate and database ecosystems. 
Currently, he focuses on the development of the ShardingSphere database middleware ecosystem and Open-Source community building.



ShardingSphere Github: <https://github.com/apache/shardingsphere>

ShardingSphere Twitter: <https://twitter.com/ShardingSphere>

ShardingSphere Slack Channel: <https://bit.ly/3qB2GGc>

Haoran's Github: <https://github.com/menghaoranss>

Haoran's Twitter: <https://twitter.com/HaoranMeng2>


