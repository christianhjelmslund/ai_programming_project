## Running AI
Run the following commands in the same directory as the Makefile. If you want to run different levels or use another arguments, open the Makefile. It should be pretty straightforward to modify.

ON MAC OR LINUX:
to clean out folder:
```make clean```


to compile project:
```make compile```


to run project (clean, compiles and runs the project):
```make run```

ON WINDOWS: 
to run project (clean, compile and run the project):
From git bash: ```sh run.sh```

## Performance Testing
To run a performance test on the current implementation place desired test levels in performance_test_levels folder and execute script performance_test. If title provided, performance test results will be stored in file test_results. If not, test results will only be printed to terminal. 

Folder for performance test levels: 
levels/performance_test_levels

To run on Mac and Linux: 
```./performance_test [Title]```

To run on Windows: 
```bash performance_test [Title]```
