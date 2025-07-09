# PeriodicFolderSaver
Console Application to periodically save (as zip) provided folder or file to provided location.
Used mostly for Games without option for multiple saves per profile (DarkSouls, NFS, GTA, CMS etc.), but not limited to games.

You can provide path to any folder, which content you want to periodically being save.
For example to automatically backup text files (of any kind), if your edit tool or environment does not provide version control functionality.

As bonus it can run related to saved files application after initial files backup in one click (if set as command in shortcut).

Usage:
(paths to folders/files better provide in double quotes (“))

Linux:
```console
java -jar "/path/to/saver/location/folder/builds/saver.jar" -c "/path/to/saver/profiles/config/file/games.list" --profile="CODE VEIN Linux"
```

Windows:
```console
java -jar "C:\path\to\saver\location\folder\builds\saver.jar" -c "C:\path\to\saver\profiles\config\file\games.list" --profile="CODE VEIN"
```

Console window can be spawned with chortcut command (specific for system and grapichical enviroment), in my case on Linux Mint 22.1 its:
```console
gnome-terminal -e 'FULL_COMMAND_GOES_HERE'
```

Full options can be seen with help option (or in Main.java):
```console
java -jar "./saver.jar" -h
```

Daemon support runtime instructions:
```console
'exit' or 'quit' - stops program execution
'save'           - manual initiation of 'standard' files archiving
'help'           - print this help (for runtime commands)
```
