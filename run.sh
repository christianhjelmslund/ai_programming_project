LEVELS_DIR=levels/comp18/
LEVEL=MAKJFWAOL.lvl # change level name
G=150 # number of graphical steps - if you want only to run it in terminal just remove "-g $G" argument
T=300 # time out in seconds 

rm -rf out
mkdir out
cd src && javac -Xlint:unchecked -cp ../lib/*:. Main.java -d ../out
cd ../out && java -jar ../server.jar -l ../$LEVELS_DIR/$LEVEL -c "java -cp ../lib/*:. Main" -g 150 -t 300
