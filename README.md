Rathings:


Aggregate Programming - Alchemist:
1) Apri il progetto con 'Open' su IntelliJ
   Quando ti si apre la maschera di Import con Gradle, spunta:
	'Use auto-import',
	'using explicit module groups'
	'Create separate module per source set'
	'Use gradle 'wrapper' task configuration'
   NB: Questi ultimi due sono consigliati dal professore, ma io non li ho messi e funziona tutto ugualmente. Tu però fai che metterli per sicurezza

2) Attendi che finisca di scaricare e configurare le librerie di base

3) Vai su File / Project Structure. 
   Quando ti si apre la maschera di Project Structure, a sinistra clicca su 'Global Libraries'
   Clicca sul pulsante + in alto a sinistra e scegli Scala SDK, seleziona una versione '2.12.X' e fai Download. 
   Quando finirà il download, scegli come modulo 'Alchemist_Example'. 
   Ora, ti si chiude la schermata download, procedi cliccando 'Apply' dopo aver verificato che lo Scala SDK si trovi ora tra le librerie importate. 
   Attendi l'Indexing in caso sia necessario.

4) Passiamo ora all'esecuzione.
   Apri il file 'src/main/scala/prova.scala'
   In alto a destra, vicino all'icona del martello per buildare, ci sarà un bottone 'Add Configuration...', cliccaci sopra e ti si aprirà un'altra maschera.
   Nella nuova schermata in alto a sinistra clicca su + e poi nel menù a tendina clicca su 'Application'. 
   Rinomina l'applicazione come vuoi e poi configurala con queste informazioni:
	Main Class: it.unibo.alchemist.Alchemist
	Program Arguments: -g src/main/resources/prova.aes -y src/main/resources/scafi.yml
	Use classpath of module: seleziona 'Alchemist_Example.main'
	Shorten command line: seleziona 'JAR Manifest'
   Ora premi 'Apply' e poi procedi.

5) Ora il tasto PLAY sarà attivo, quindi esegui e controlla che la configurazione abbia avuto successo. 
   Tutto è stato configurato correttamente se ti apparirà una nuova schermata Alchemist Simulator con tanti puntini immobili. 
   Schiacciando il tasto P sulla tastiera, partirà l'esecuzione e se rischiacci lo stesso tasto la metti in pausa.

PROBLEMA: 
Durante il passo 4 ti ho detto di mettere come Program Arguments '-g src/main/resources/prova.aes [...]'. 
In realtà in resources questo file non esiste (neanche nella repository), infatti quando esegui, anche se si apre l'Alchemist Simulator, la console darà un errore.
Torna quindi dove avevi cliccato 'Add Configuration...' (dove ora ci sarà il nome dell'applicazione che hai creato al passo 4) e facendo 'Edit Configurations'. 
Ora in 'Program Arguments' sostituisci 'prova.aes' con 'example.aes'. Ora prova a rieseguire il tutto.

Questo nuovo file è presente in resources ed attiva gli EFFETTI GRAFICI per l'esecuzione. Visto che non c'è il file proposto dal professore, ipotizzo che non siano importanti.
Ma magari informiamoci!