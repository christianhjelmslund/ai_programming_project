SRC_DIR = src
CLASS_DIR = out
LEVELS_DIR = levels/performance_test_levels
LEVEL = MALobot.lvl # change level name
G = 150 # number of graphical steps - if you want only to run it in terminal just remove "-g" argument
T = 180 # time out in seconds

.PHONY: clean
clean:
	rm -rf out

.PHONY: compile
compile: clean
	mkdir $(CLASS_DIR)
	cd $(SRC_DIR) && javac -cp .:../lib/guava-28.2-jre.jar Main.java -d ../$(CLASS_DIR)

.PHONY: run
run: clean compile
	cd out && java -jar ../server.jar -g -l ../$(LEVELS_DIR)/$(LEVEL) -c "java -cp .:../lib/guava-28.2-jre.jar Main" -t $(T)

