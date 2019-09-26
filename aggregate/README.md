# Esercitazione su Alchemist

## Installazione e preparazione

1)	Scarica il progetto di esempio, disponibile al seguente indirizzo:
	[bitbucket.org/gaudrito/alchemist-example](https://bitbucket.org/gaudrito/alchemist-example)

2)	Installa la virtual machine Java SE 8 JDK:
	[oracle.com/technetwork/java/javase/downloads](http://oracle.com/technetwork/java/javase/downloads)

3)	Installa l'editor integrato IntelliJ (versione Community JBR 8):
	[jetbrains.com/idea/download](https://www.jetbrains.com/idea/download)

Durante l'installazione, seleziona il plugin per il supporto a Scala.

4)	Installa l'editor integrato IntelliJ (versione Community JBR 8):
	[jetbrains.com/idea/download](https://www.jetbrains.com/idea/download)

5)	Apri IntelliJ e clicca su `File > Open`. Seleziona il file `build.gradle` presente nella cartella con il progetto di esempio scaricato.
	Clicca sul pulsante `Open as Project` e poi compila il form scegliendo "explicit module groups" e "Gradle 'wrapper' task configuration".

6)	Apri il file `src/main/prova.scala`. Comparirà un banner giallo in alto, su cui puoi cliccare `Setup Scala SDK`.
	Se non comparisse, cerca la voce di menu `File > Project Structure > Global Libraries`.
	Compariranno poi delle finestre in cui potrai cliccare `Create > Download`, selezionare una versione `2.12.x`
	(per qualunque `x`) e poi premere `OK`. Attendi che lo scaricamento termini e poi premi ancora `OK`.

7)	Esegui il progetto di esempio per accertarti che tutto abbia funzionato. Clicca su `Add Configuration > + > Application` e poi imposta:

* Main class: `it.unibo.alchemist.Alchemist`

* Program arguments: `-g src/main/resources/prova.aes -y src/main/resources/scafi.yml`

* Use classpath of module: `Alchemist_Example_main`

* Shorten command line: `JAR manifest` 
    
Premi quindi `OK` e poi il tasto play verde. Dopo qualche secondo si aprirà l'interfaccia di Alchemist,
e potrai premere la lettera `P` per avviare e fermare la simulazione.

Vedi il file `README.pdf` per ulteriori dettagli sul processo di preparazione.

## Il progetto di esempio

Vedi il file `README.pdf` per ulteriori dettagli sul progetto di esempio.

## Esercizi proposti

Modificate il file `prova.pt` del progetto "Alchemist Example", incrementalmente,
per calcolare in ogni device le seguenti cose.

1)	il numero di device vicini

2)	il massimo numero di vicini che il device corrente ha mai avuto

3)	il massimo numero di vicini che un qualunque device della rete ha mai avuto

4)	muoversi verso il vicino che ha meno vicini

5)	muoversi lontano dal vicino che ha più vicini

6)	combinare gli ultimi due punti: ogni device è attratto dal vicino con meno vicini,
	e respinto dal vicino con più vicini.

### Suggerimenti

*	Nei primi esercizi, ragionate dove usare `nbr` ("guardo i vicini") e `rep` ("guardo il passato").

*	Per muovere un device, bisogna memorizzare una posizione `List(x,y)` nella molecola `target`:

``node.put("target", ...);``

Sostituite la porzione di codice che ora si occupa del movimento con quanto richiesto.

*	Si può ottenere la posizione corrente di un device tramite il metodo `getCoordinates()`.

## Risorse utili

Vedi il file `README.pdf` per link a ulteriori risorse.
