LEVELS_DIR=levels/new_levels/
LEVEL=MAOpenUpMed.lvl # change level name
G=150 # number of graphical steps - if you want only to run it in terminal just remove "-g $G" argument
T=180 # time out in seconds

rm -rf out
javac -cp "src;lib/guava-28.2-jre.jar" src/Main.java -d out
cd out && java -jar ../server.jar -l ../$LEVELS_DIR/$LEVEL -c "java -cp ../out;../lib/guava-28.2-jre.jar Main " -g $G  -t $T
