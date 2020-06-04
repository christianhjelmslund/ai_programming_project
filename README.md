## Running the client
Run the following commands in the same directory as the `Makefile` (if on Windows, `run.sh`). If you want to run different levels or use other arguments, open the Makefile. You can change the $LEVEL parameter on line 5, if you want to test another level. Just make sure it is in the correct levels/comp20 directory, otherwise modify that as well.

ON MAC OR LINUX:
to clean out folder:

```make clean```

to compile project:

```make compile```

to run project (clean, compiles and runs the project):

```make run```

to run all the competition levels (be aware that the generated iioo.zip file will be in the out folder):

```make competition```

ON WINDOWS: 
to run project (clean, compile and run the project):
From git bash: ```sh run.sh```

## Performance Testing
To run a performance test on the current implementation place desired test levels in performance_test_levels folder and execute script performance_test. If title provided, performance test results will be stored in file test_results. If not, test results will only be printed to terminal. 

Folder for performance test levels: 
```levels/performance_test_levels```

To run on Mac and Linux: 
```./performance_test [Title]```

To run on Windows: 
```bash performance_test [Title]```

## Project Structure

The only two folders which are *actually needed* are `src` and `lib`. The rest are for "convenience".

`levels` directory contains levels in which you can use to test the client.

`Makefile` is to create run configurations for unix systems.

`run.sh` is to create run configurations for Windows systems (and unix)

`performance_test` is to run all levels in the `levels/performance_test_levels` directory, to be able to easy test the changes made.

`server.jar` is the server that the client writes to

`test_results` a text file containing the results of the performance tests
