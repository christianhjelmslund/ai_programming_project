Run the following commands in the same directory as the Makefile.

clean out folder
```make clean```

compile project
```make compile```

run project (clean, compiles and runs the project)
```make run```


Arguments for the server.jar file:

Available options (requires either -c and -l, or -o <file>)
Option                                  Description                            
------                                  -----------                            
-?, --help                              Print help                             
-c, --client <command>                  Command to execute planning client     
-g, --gui [Integer: State]              Show level graphically. State is       
                                          either  < 10 (fastest), or greater   
                                          than 10 (animation time of actions)  
-l, --level <path>                      Path to level file                     
-o, --logging [directory or file]       Plays log if file (ignores -c and -l), 
                                          otherwise writes log to directory    
                                          (default: .)                         
-p, --pause                             Paused at startup                      
-t, --timeout <Integer: s>              Timeout. -1 = no timeout (default: -1) 