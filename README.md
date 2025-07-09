# PeriodicFolderSaver
Console application to periodically save (as ZIP) a specified folder or file to a target location.
Originally designed for games that don’t support multiple save profiles (e.g., Dark Souls, NFS, GTA, CMS), but not limited to gaming use.

You can use it to back up any folder whose contents you want to periodically save.
For example, it can automatically archive text files, project folders, or other data that your editing tool or environment doesn't version automatically.

Optionally, the program can run an application (e.g., the game or editor) right after performing the initial backup – ideal for launching via a shortcut with one click.

Usage:
It's recommended to wrap paths in double quotes (") to avoid issues with spaces or special characters.

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
