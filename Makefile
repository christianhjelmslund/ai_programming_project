SRC_DIR = src
CLASS_DIR = out
LEVELS_DIR = levels
LEVEL = SAD1.lvl # change level name
G = 150 # number of graphical steps - if you want only to run it in terminal just remove "-g" argument
T = 300 # time out in seconds 

.PHONY: cleangi
clean:
	rm -rf out

.PHONY: compile
compile: clean
	mkdir $(CLASS_DIR)
	cd $(SRC_DIR) && javac Main.java -d ../$(CLASS_DIR)

.PHONY: run
run: clean compile
	cd out && java -jar ../server.jar -l ../$(LEVELS_DIR)/$(LEVEL) -c "java Main" -g $(G) -t $(T)