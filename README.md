# SpectatorPlus

####Quick links · [BukkitDev](http://dev.bukkit.org/bukkit-plugins/spectator/) · [Jenkins Builds](https://ci.pgmann.cf/job/SpectatorPlus/) · [JavaDoc](https://ci.pgmann.cf/job/SpectatorPlus/javadoc?com/pgcraft/spectatorplus/SpectateAPI.html) · [Maven Repo](https://mvn.pgmann.cf/#browse/browse/components) · [License](https://www.mozilla.org/MPL/2.0/)
---

SpectatorPlus is a plugin for Bukkit. For more information and guides on how to use it, [see the BukkitDev page ](http://dev.bukkit.org/bukkit-plugins/spectator/).

#### ~ License ~

This project is licensed under the [Mozilla Public License v2.0 (MPLv2)](https://www.mozilla.org/MPL/2.0/).  
A summary of this licence is [available here](https://tldrlegal.com/license/mozilla-public-license-2.0-(mpl-2)#summary).

#### ~ Development ~

The source code of this repository is automatically built and [is always available here](https://ci.pgmann.cf/job/SpectatorPlus/).
Older development builds [can be obtained here](http://jenkins.carrade.eu/job/SpectatorPlus/) instead. You can find [the API's JavaDoc here](https://ci.pgmann.cf/job/SpectatorPlus/javadoc?com/pgcraft/spectatorplus/SpectateAPI.html). In order to build this project, it is recommended to use Maven. If you don't, you will need to import the various dependencies manually, such as [Apache Commons Lang](http://commons.apache.org/proper/commons-lang/), to be able to build the project.

#### ~ API Usage ~

If you want to integrate SpectatorPlus with your own project, you can either directly import a downloaded `.jar` into your IDE, or if you use Maven, you can reference [my Maven Repository](https://mvn.pgmann.cf/#browse/browse/components) in your `pom.xml` to have the correct version downloaded automatically during building. The advantage of this is that maven will automatically find new updates and download them from my repository.

    <project ...>
        <repositories>
            <repository>
                <id>mvn-pgmann</id>
                <name>pgmann's repository</name>
                <url>https://mvn.pgmann.cf/</url>
            </repository>
        </repositories>
        <dependencies>
            <dependency>
                <groupId>com.pgcraft</groupId>
                <artifactId>SpectatorPlus</artifactId>
                <version>LATEST</version>
            </dependency>
        </dependencies>
    </project>
