LEVELS_DIR=levels/comp18/
LEVEL=MAbongu.lvl # change level name
G=150 # number of graphical steps - if you want only to run it in terminal just remove "-g $G" argument
T=180 # time out in seconds

rm -rf out
javac -cp "src;lib/guava-28.2-jre.jar" src/Main.java -d out
cd out && java -jar ../server.jar -l ../$LEVELS_DIR/$LEVEL -c "java -cp ../out;../lib/guava-28.2-jre.jar Main " -g $G  -t $T

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
# MAora - TODO: Antiblock, Assign agents to boxes
# MAZEROagent - TODO: Gensidig Rækkefølge + antiblock "MARKED"

# DECENTRALIZED: Løs MAEasyPeasy og MABahaMAS




# ________ Notes For optimal heuristic ______________
# TODO: GreenDots performs a lot better without assignment of boxes to goals!
# TODO: Optimer initiel tildeling af boxes til goals. Eksempelvis med PREDICATE/FILTER: Skal indeholde alle bogstaver? The Hungarian Algorithm!!!
# TODO: Somehow guide agents away from vital paths ... Larger punishment the longer into the "valley?"
# TODO: Find en måde at få agenter til at gå uden om bokse, selvom den korteste vej er igennem
# TODO: Kun minimér afstand fra bokse til mål når agenters tur
# TODO: Several red agents should never go to same box (assign agents to boxes)


