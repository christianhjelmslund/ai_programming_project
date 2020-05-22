LEVELS_DIR=levels/comp20/
LEVEL=MAIIOO.lvl # change level name


G=150 # number of graphical steps - if you want only to run it in terminal just remove "-g $G" argument
T=180 # time out in seconds

rm -rf out
cd src
javac -cp "./;../lib/guava-28.2-jre.jar" Main.java -d ../out
cd ../out && java -jar ../server.jar -l ../$LEVELS_DIR/$LEVEL -c "java -cp ./;../lib/guava-28.2-jre.jar Main" -g $G  -t $T


#mkdir out
#cd src
#javac -cp "./;../lib/guava-28.2-jre.jar" Main.java -d ../out
#cd ../out && java -jar ../server.jar -l ../levels/comp20SOLV/MAIIOO.lvl -c "java -cp ./;../lib/guava-28.2-jre.jar Main" -t 180

mkdir out
cd src && javac -cp "./;../lib/guava-28.2-jre.jar" Main.java -d ../out
cd ../out && java -jar ../server.jar -l ../levels/comp20 -c "java -cp ./;../lib/guava-28.2-jre.jar Main" -t 180 -o "ioio.zip"

# _______Unsolvable Levels from comp20_________
# MAaiaicapn - kræver at box hives væk fra et mål først - TODO: antiblock -ish (O CHECK DEPENDENCY)
# MABoxAgents - TODO: (easy!) antiblock (O CHECK DEPENDENCY)
# MAfootsteps - TODO: Antiblock. Fjern bokse fra korridor, før vi begynder at putte bokse ind i rækkefølge
# MANicolAI - TODO: Mere sofistikeret rækkefølge
# MAThree - TODO: Mere sofistikeret rækkefølge




# MAAIstars - rækkefølge af både bokse og agenter - TODO: DECENTRALIZED Agentgoals + order of those JA
# MADeepPlan - TODO: DECENTRALIZED NEJ
# MAFPHPOP - TODO: DECENTRALIZED (hard) + antiblock (9 agents) NEJ
# MAKaren - TODO: DECENTRALIZED MÅSKE
# MAReftAI - TODO: DECENTRALIZED, (Easy?!) assign objectives correctly? MÅSKE
# MASokoBros - TODO: DECENTRALIZED, (hard) needs unblock NEJ
# MATheZoo - TODO: DECENTRALIZED Relativ simpel hvis vi håndterer konflikter
# MAVAikings - TODO: DECENTRALIZED, konflikthåndtering





# _______Unsolvable Levels from comp18_________
# MAAlphaOne
# MAAntsStar - TODO: indbyg rækkefølge (evt. straf boxes og agents som er i korridor før deres tur)
# MABahaMAS - hukommelsestungt pga. 10 agenter VH.. prune? DECENTRALIZED
# MAByteMe - justering af vægte, TODO: Unblock
# MACybot - justering af vægte, TODO: Remove agents from vital paths
# MADashen - justering af vægte, tjek også agent 2's opførsel? TODO: Unblock
# MADaVinci - Kræver mere sofistikeret heuristik for at åbne for agent 0, TODO: Antiblok
# MAEasyPeasy - Find alternativ rute uden om bokse, selvom det er den korteste rute: TODO: Impl. alternativ vej udenom boks
# MAKaldi - Next level : for mange agenter DECENTRALIZED
# MAKarlMarx - Next level : for mange agenter og bokse. DECENTRALIZED
# MANavy - Decentralize
# MANotHard - Decentralize
# MAora - TODO: Antiblock, Assign agets to boxes
# MAZEROagent - TODO: Gensidig Rækkefølge + antiblock "MARKED"

# DECENTRALIZED: Løs MAEasyPeasy og MABahaMAS



