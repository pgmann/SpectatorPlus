# SpectatorPlus

####Quick links · [BukkitDev](http://dev.bukkit.org/bukkit-plugins/spectator/) · [Jenkins Builds](https://ci.pgmann.cf/job/SpectatorPlus/) · [JavaDoc](https://ci.pgmann.cf/job/SpectatorPlus/javadoc?com/pgcraft/spectatorplus/SpectateAPI.html) · [Maven Repo](https://mvn.pgmann.cf/) · [License](https://www.mozilla.org/MPL/2.0/)
---

SpectatorPlus is a plugin for Bukkit. For more information and guides on how to use it, [see the BukkitDev page ](http://dev.bukkit.org/bukkit-plugins/spectator/).

#### ~ License ~

This project is licensed under the [Mozilla Public License v2.0 (MPLv2)](https://www.mozilla.org/MPL/2.0/).  
A summary of this licence is [available here](https://tldrlegal.com/license/mozilla-public-license-2.0-(mpl-2)#summary).

#### ~ Obtaining a Build ~

The source code of this repository is automatically built and [is always available here](https://ci.pgmann.cf/job/SpectatorPlus/).
Older, legacy development builds [can be obtained here](http://jenkins.carrade.eu/job/SpectatorPlus/) instead. In order to build this project yourself, it is recommended to use **Maven**. If you don't, you will need to import the various dependencies manually, such as [Apache Commons Lang](http://commons.apache.org/proper/commons-lang/), to be able to build the project.

#### ~ API Usage ~

If you want to integrate SpectatorPlus with your own project, you can either directly import a downloaded `.jar` into your IDE, or if you use Maven, you can reference [my Maven Repository](https://mvn.pgmann.cf/) in your `pom.xml` to have the correct version downloaded automatically during building. The advantage of this is that maven will automatically find new updates and download them from my repository.

```xml
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
            <version>B3.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```
You can prefix the dependency version with either an `S` for Stable (Release) or a `B` for Beta (Development). Beta builds need to be postfixed with `-SNAPSHOT`. The Maven Repository stores builds from `B3.0-SNAPSHOT` and onwards.

Once SpectatorPlus is set up as a dependency of your project, you can start using the API.

```java
// Ensure SpectatorPlus is loaded
Plugin spTest = Bukkit.getServer().getPluginManager().getPlugin("SpectatorPlus");
if(spTest == null || !spTest.isEnabled()) return;

// Get a reference to the public API
SpectateAPI spApi = SpectateAPI.getAPI();

// Start using it!
if(spApi.isSpectator(player)) { // Check if 'player' is spectating
    // ...
}
spApi.setSpectating(player, true); // Turn on/off spectate mode for 'player'
Toggles.TOOLS_TELEPORTER_ENABLED.set(true); // Edit the values of each item in toggles.yml

```
For detailed usage information on how to use the API, you can find [its JavaDoc here](https://ci.pgmann.cf/job/SpectatorPlus/javadoc?com/pgcraft/spectatorplus/SpectateAPI.html).
