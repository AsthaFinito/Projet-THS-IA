import java.util.Arrays;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import FFT.*;
import Neurone.*;
import Son.*;
import javax.sound.sampled.AudioFormat;

public class DetecteurDeSon2 {
    static Complexe[][][] listeFFTGuess;
    static Complexe[][][] listeTrain;
    static Complexe[][][] listeTest;
    private static final int TAILLE_FFT = 512;
    private static iNeurone[] neurones;

    public static void main(String[] args) {
        Son[] allSons = initFichierSon();// Initialiser les fichiers son
        initialiserNeurones(allSons.length-1, TAILLE_FFT); // Initialiser les neurones
        final int[] nbBloc = new int[allSons.length];
        listeFFTGuess = new Complexe[allSons.length][][];
        fftForAllSons(allSons, nbBloc); // Effectuer la FFT pour tous les sons
        listeTrain = new Complexe[allSons.length][][];
        listeTest = new Complexe[allSons.length][][];

         // Séparer les données en ensembles d'entraînement et de test
        for (int i = 0; i < allSons.length; i++) { 
            int nbBlocsTrain = (int) (nbBloc[i] * 0.8);
            int nbBlocsTest = nbBloc[i] - nbBlocsTrain;
            listeTrain[i] = Arrays.copyOfRange(listeFFTGuess[i], 0, nbBlocsTrain);
            listeTest[i] = Arrays.copyOfRange(listeFFTGuess[i], nbBlocsTrain, nbBlocsTrain + nbBlocsTest);
        }

        float moyenne = 0;
        // for (int tour = 0; tour < 50; tour++) {
        //     

        //     System.out.println("Lecture du fichier WAV " + args[0]);
        //     Son sonToPredict = new Son(args[0]);
        //     int nbBlocPredict = sonToPredict.donnees().length / TAILLE_FFT;
        //     Complexe[][] signalToPredict = fftSur1Son(sonToPredict, nbBlocPredict);
        //     moyenne += prediction(signalToPredict, neurones[0], nbBlocPredict);
        // }
        //System.out.println("Fin de statistique sur " + 50 + " tours avec " + moyenne / 50 + " de précision pour " + args[0]);
        //entrainerTousLesNeurones();

        // opérations d'entraînement et de prédiction
        Complexe[][] verifNeuroneC = entrainerEtPredirePour1Neurone(0, 1, 0);
        hardcodeEntrainementSinus();
        hardcodeEntrainementSinus2();
        hardcodeEntrainementSinus3();
        if (args.length > 0) {
            
            predireSurFichier(args[0]);
        }


        //testerNeuroneSurTousLesSons(neurones[0],allSons);
    }

    // Initialise les neurones avec le nombre et la taille des entrées spécifiées
    private static void initialiserNeurones(int nombreDeNeurones, int tailleDesEntrees) {
        neurones = new iNeurone[nombreDeNeurones];
        for (int i = 0; i < nombreDeNeurones; i++) {
            neurones[i] = new NeuroneHeaviside(tailleDesEntrees);
        }
    }

    // Entraîner et prédire pour un neurone donné
    private static Complexe[][] entrainerEtPredirePour1Neurone(int indexTrain1, int indexTrain2,int indexNeurone) {
        // Fusionner les signaux d'entraînement (hardcode pour l'instant)
        Complexe[][] entreeNeuroneC = listeTrain[0];
        int nombreDeFusions = 1;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[1]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[2]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[3]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[4]);
        nombreDeFusions++;
        
       
        // Fusionner les signaux de vérification
        Complexe[][] verifNeuroneC = fusion(listeTest[indexTrain1], listeTest[indexTrain2]);

        // Convertir les données d'entraînement en format approprié pour le neurone
        float[][] entreeNeuroneF = convFFTtoEntree(entreeNeuroneC, entreeNeuroneC.length, TAILLE_FFT);
        entreeNeuroneF = normaliserDonnees(entreeNeuroneF);

        // Calculer les proportions pour le tableau de résultats
        int taille = entreeNeuroneF.length;
        int nbPositifs = taille / nombreDeFusions;

        // Créer le tableau de résultats
        float[] resultat = creerTableau(taille, nbPositifs);

        System.out.println(resultat.length);
        System.out.println(entreeNeuroneF.length);
        System.out.println("Nombre de tours : " + neurones[0].apprentissage(entreeNeuroneF, resultat));

        //lireSynapseEtBiais();

        prediction(verifNeuroneC, neurones[indexNeurone], verifNeuroneC.length);
        return verifNeuroneC;
    }

    // Entraînement hardcodé pour le sinus
    private static Complexe[][] hardcodeEntrainementSinus() {
        // Fusionner les signaux d'entraînement (hardcode pour l'instant)
        Complexe[][] entreeNeuroneC = listeTrain[1];
        int nombreDeFusions = 1;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[0]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[2]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[3]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[4]);
        nombreDeFusions++;
       
        // Fusionner les signaux de vérification
        Complexe[][] verifNeuroneC = fusion(listeTest[1], listeTest[3]);

        // Convertir les données d'entraînement en format approprié pour le neurone
        float[][] entreeNeuroneF = convFFTtoEntree(entreeNeuroneC, entreeNeuroneC.length, TAILLE_FFT);
        entreeNeuroneF = normaliserDonnees(entreeNeuroneF);

        // Calculer les proportions pour le tableau de résultats
        int taille = entreeNeuroneF.length;
        int nbPositifs = taille / nombreDeFusions;

        // Créer le tableau de résultats
        float[] resultat = creerTableau(taille, nbPositifs);

        System.out.println(resultat.length);
        System.out.println(entreeNeuroneF.length);
        System.out.println("Nombre de tours : " + neurones[1].apprentissage(entreeNeuroneF, resultat));

        //lireSynapseEtBiais();

        prediction(verifNeuroneC, neurones[1], verifNeuroneC.length);
        return verifNeuroneC;
    }

    // Entraînement hardcodé pour un sinus V2
    private static Complexe[][] hardcodeEntrainementSinus2() {
        // Fusionner les signaux d'entraînement (hardcode pour l'instant)
        Complexe[][] entreeNeuroneC = listeTrain[2];
        int nombreDeFusions = 1;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[0]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[1]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[3]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[4]);
        nombreDeFusions++;
       
        // Fusionner les signaux de vérification
        Complexe[][] verifNeuroneC = fusion(listeTest[1], listeTest[3]);

        // Convertir les données d'entraînement en format approprié pour le neurone
        float[][] entreeNeuroneF = convFFTtoEntree(entreeNeuroneC, entreeNeuroneC.length, TAILLE_FFT);
        entreeNeuroneF = normaliserDonnees(entreeNeuroneF);

        // Calculer les proportions pour le tableau de résultats
        int taille = entreeNeuroneF.length;
        int nbPositifs = taille / nombreDeFusions;

        // Créer le tableau de résultats
        float[] resultat = creerTableau(taille, nbPositifs);

        System.out.println(resultat.length);
        System.out.println(entreeNeuroneF.length);
        System.out.println("Nombre de tours : " + neurones[2].apprentissage(entreeNeuroneF, resultat));

        //lireSynapseEtBiais();

        prediction(verifNeuroneC, neurones[2], verifNeuroneC.length);
        return verifNeuroneC;
    }
    private static Complexe[][] hardcodeEntrainementSinus3() {
        // Fusionner les signaux d'entraînement (hardcode pour l'instant)
        Complexe[][] entreeNeuroneC = listeTrain[3];
        int nombreDeFusions = 1;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[0]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[1]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[2]);
        nombreDeFusions++;
        entreeNeuroneC = fusion(entreeNeuroneC, listeTrain[4]);
        nombreDeFusions++;
        
       
        // Fusionner les signaux de vérification
        Complexe[][] verifNeuroneC = fusion(listeTest[1], listeTest[3]);

        // Convertir les données d'entraînement en format approprié pour le neurone
        float[][] entreeNeuroneF = convFFTtoEntree(entreeNeuroneC, entreeNeuroneC.length, TAILLE_FFT);
        entreeNeuroneF = normaliserDonnees(entreeNeuroneF);

        // Calculer les proportions pour le tableau de résultats
        int taille = entreeNeuroneF.length;
        int nbPositifs = taille / nombreDeFusions;

        // Créer le tableau de résultats
        float[] resultat = creerTableau(taille, nbPositifs);

        System.out.println(resultat.length);
        System.out.println(entreeNeuroneF.length);
        System.out.println("Nombre de tours : " + neurones[3].apprentissage(entreeNeuroneF, resultat));

        //lireSynapseEtBiais();

        prediction(verifNeuroneC, neurones[3], verifNeuroneC.length);
        return verifNeuroneC;
    }

    

    // Entraîner tous les neurones
    private static void entrainerTousLesNeurones() {
        for (int i = 0; i < neurones.length; i++) {
            int indexTrain1 = i; 
            int indexTrain2 = 5; // harcode
            System.out.println("Entraînement du neurone " + i + " avec les indices " + indexTrain1 + " et " + indexTrain2);
            entrainerEtPredirePour1Neurone(indexTrain1, indexTrain2, i);
        }
    }

    // Lire les synapses et biais de tous les neurones
    private static void lireSynapseEtBiais() {
        for (int i = 0; i < neurones.length; i++) {
            if (neurones[i] instanceof Neurone) {
                Neurone neurone = (Neurone) neurones[i];
                float[] synapses = neurone.synapses();
                System.out.print("Neurone " + i + " - Longueur des synapses : " + synapses.length);
                // for (float f : neurone.synapses())
                //     System.out.print(f + " ");
                System.out.print("\nBiais : ");
                System.out.println(neurone.biais());
            }
        }
    }

    // Initialisation des sons
    public static Son[] initFichierSon() {
        String[] cheminsFichiers = {
            "./Sources sonores/Carre.wav",
            "./Sources sonores/Sinusoide.wav",
            "./Sources sonores/Sinusoide2.wav",
            "./Sources sonores/Sinusoide3Harmoniques.wav",
            //"./Sources sonores/Combinaison.wav",
            "./Sources sonores/Bruit.wav"
        };

        Son[] sons = new Son[cheminsFichiers.length];
        for (int i = 0; i < cheminsFichiers.length; i++) {
            sons[i] = new Son(cheminsFichiers[i]);
        }

        return sons;
    }

    // Prédire sur un fichier donné
    private static void predireSurFichier(String nomFichier) {
        System.out.println("Lecture du fichier WAV " + nomFichier);
        Son sonToPredict = new Son(nomFichier);
        int nbBlocPredict = sonToPredict.donnees().length / TAILLE_FFT;
        Complexe[][] signalToPredict = fftSur1Son(sonToPredict, nbBlocPredict);
    
        float sommeSortiesNeurone0 = 0;
        float sommeSortiesNeurone1 = 0;
        float sommeSortiesNeurone2 = 0;
        float sommeSortiesNeurone3 = 0;
        for (int i = 0; i < 50; i++) {
            System.out.println("Tour numéro " + i);
            sommeSortiesNeurone0 += prediction(signalToPredict, neurones[0], nbBlocPredict);
            sommeSortiesNeurone1 += prediction(signalToPredict, neurones[1], nbBlocPredict);
            sommeSortiesNeurone2 += prediction(signalToPredict, neurones[2], nbBlocPredict);
            sommeSortiesNeurone3 += prediction(signalToPredict, neurones[3], nbBlocPredict);
        }
    
        float moyenneSortiesNeurone0 = sommeSortiesNeurone0 / 50;
        float moyenneSortiesNeurone1 = sommeSortiesNeurone1 / 50;
        float moyenneSortiesNeurone2 = sommeSortiesNeurone2 / 50;
        float moyenneSortiesNeurone3 = sommeSortiesNeurone3 / 50;
        System.out.println("Moyenne des sorties pour le neurone 0  avec "+50+" : " + moyenneSortiesNeurone0);
        System.out.println("Moyenne des sorties pour le neurone 1  avec "+50+" : " + moyenneSortiesNeurone1);
        System.out.println("Moyenne des sorties pour le neurone 2  avec "+50+" : " + moyenneSortiesNeurone2);
        System.out.println("Moyenne des sorties pour le neurone 3  avec "+50+" : " + moyenneSortiesNeurone3);
    }
    
    // Effectuer la FFT pour un son
    private static Complexe[][] fftSur1Son(Son son, int nbBloc) {
        int tailleBloc = TAILLE_FFT;
        Complexe[][] fft = new Complexe[nbBloc][tailleBloc];

        for (int i = 0; i < nbBloc; i++) {
            float[] bloc = son.bloc_deTaille(i, tailleBloc);
            fft[i] = fftBloc(bloc);
        }

        return fft;
    }

    
    // Effectuer la FFT sur un bloc de données
private static Complexe[] fftBloc(float[] bloc) {
    // Convertir le bloc de données en un tableau de nombres complexes
    Complexe[] signalComplexe = new Complexe[bloc.length];
    for (int i = 0; i < bloc.length; i++) {
        signalComplexe[i] = new ComplexeCartesien(bloc[i], 0); // Créer un nombre complexe avec la partie imaginaire nulle
    }

    // Appliquer la FFT sur le tableau de nombres complexes
    return FFTCplx.appliqueSur(signalComplexe);
}

// Effectuer la FFT pour tous les sons
private static void fftForAllSons(Son[] allSons, int[] nbBloc) {
    for (int i = 0; i < allSons.length; i++) {
        // Calculer le nombre de blocs pour chaque son
        nbBloc[i] = allSons[i].donnees().length / TAILLE_FFT;
        // Effectuer la FFT sur chaque son et stocker les résultats
        listeFFTGuess[i] = fftSur1Son(allSons[i], nbBloc[i]);
    }
}

// Lire tous les blocs de taille fixe d'un signal
public static Complexe[][] lireTousLesXBlocs(int tailleBloc, float[] donnees, double pourcentage) {
    int tailleSignal = donnees.length;
    // Calculer le nombre de blocs à extraire en fonction du pourcentage
    int nombreTotalBlocs = (int) (tailleSignal / tailleBloc * pourcentage);
    // Initialiser un tableau pour stocker tous les blocs de Complexe
    Complexe[][] tousLesBlocs = new Complexe[nombreTotalBlocs][];

    // Parcourir tous les blocs
    for (int i = 0; i < nombreTotalBlocs; i++) {
        // Lire chaque bloc de données
        float[] bloc = bloc_deTaille(i, tailleBloc, donnees);
        // Convertir chaque bloc en Complexe et l'ajouter au tableau
        tousLesBlocs[i] = convertirEnComplexe(bloc);
    }

    // Retourner tous les blocs de Complexe
    return tousLesBlocs;
}

// Lire un bloc de données d'une taille spécifiée
public static float[] bloc_deTaille(final int numeroBloc, final int tailleBloc, float[] donnees) {
    final int from = numeroBloc * tailleBloc; // Début du bloc
    final int to = from + tailleBloc; // Fin du bloc
    // Retourner une copie du bloc spécifié
    return Arrays.copyOfRange(donnees, from, to);
}

// Convertir un bloc de données en nombres complexes
public static Complexe[] convertirEnComplexe(float[] bloc) {
    // Initialiser un tableau de nombres complexes
    Complexe[] signalComplexe = new Complexe[bloc.length];
    for (int i = 0; i < bloc.length; i++) {
        // Créer un nombre complexe pour chaque élément du bloc
        signalComplexe[i] = new ComplexeCartesien(bloc[i], 0);
    }
    // Retourner le tableau de nombres complexes
    return signalComplexe;
}


    // Convertir la FFT en données d'entrée pour le réseau de neurones 
    private static float[][] convFFTtoEntree(Complexe[][] signalTrain, int nbBloc, int taille) {
        // Conversion de notre signal à notre entrée de neurone
        float[][] entrees = new float[nbBloc][taille];
        for (int i = 0; i < nbBloc; i++) {
            for (int j = 0; j < taille; j++) {
                entrees[i][j] = (float) signalTrain[i][j].mod();
            }
        }
        return entrees;
    }

    // Fusionner deux ensembles de données FFT
    public static Complexe[][] fusion(Complexe[][] matrice1, Complexe[][] matrice2) {
        int totalRows = matrice1.length + matrice2.length;
        int cols = matrice1[0].length;
        Complexe[][] fusion = new Complexe[totalRows][cols];

        for (int i = 0; i < matrice1.length; i++) {
            fusion[i] = matrice1[i];
        }

        for (int i = 0; i < matrice2.length; i++) {
            fusion[matrice1.length + i] = matrice2[i];
        }

        return fusion;
    }

    // Normaliser les données
    private static float[][] normaliserDonnees(float[][] donnees) {
        int n = donnees.length; // Nombre de lignes (nombre de vecteurs)
        int m = donnees[0].length; // Nombre de colonnes (nombre de caractéristiques par vecteur)
        float[][] normalisees = new float[n][m];

        for (int i = 0; i < n; i++) {
            // Trouver le min et le max de l'amplitude uniquement
            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;
            for (int j = 0; j < m; j++) {
                if (donnees[i][j] < min) min = donnees[i][j];
                if (donnees[i][j] > max) max = donnees[i][j];
            }

            float range = max - min;

            // Vérifier si le range est non nul
            if (range != 0) {
                // Normaliser les amplitudes seulement
                for (int j = 0; j < m; j++) {
                    normalisees[i][j] = (donnees[i][j] - min) / range;
                }
            } else {
                // Si le range est nul, réinitialiser toutes les amplitudes à zéro
                for (int j = 0; j < m; j++) {
                    normalisees[i][j] = 0;
                }
            }
        }
        return normalisees;
    }

    // Créer un tableau de résultats
    public static float[] creerTableau(int taille, int nbPositifs) {
        float[] tableau = new float[taille];

        for (int i = 0; i < nbPositifs; i++) {
            tableau[i] = 1.0f;
        }

        for (int i = nbPositifs; i < taille; i++) {
            tableau[i] = 0.0f;
        }

        System.out.println("Tableau créé avec " + nbPositifs + " positifs et " + (taille - nbPositifs) + " négatifs.");
        return tableau;
    }

    // Prédire et afficher les résultats
    public static float prediction(Complexe[][] signalTrain, iNeurone nCarre, int nbBloc) {
        // Conversion de notre signal à notre entrée de neurone
        final int taille = signalTrain[0].length;
        float[][] entrees = convFFTtoEntree(signalTrain, nbBloc, taille);
    
        // Normalisation des valeurs
        entrees = normaliserDonnees(entrees);
    
        float sommeSorties = 0;
    
        // On affiche chaque cas appris
        for (int i = 0; i < nbBloc; i++) {
            // Pour une entrée donnée
            final float[] entree = entrees[i];
            // On met à jour la sortie du neurone
            nCarre.metAJour(entree);
            // On récupère cette sortie
            float sortie = nCarre.sortie();
            // On ajoute cette sortie à la somme des sorties
            sommeSorties += sortie;
            // On affiche cette sortie
            //System.out.println("Entree " + i + " : " + sortie);
        }
    
        // Calcul de la moyenne des sorties
        float moyenneSorties = sommeSorties / nbBloc;
        // Affichage de la moyenne
        System.out.println("Moyenne des sorties : " + moyenneSorties);
        return moyenneSorties;
    }
}

class Son {
    private int frequence;
    private float[] donnees;

    public int frequence() {
        return frequence;
    }

    public float[] donnees() {
        return donnees;
    }

    public float[] bloc_deTaille(final int numeroBloc, final int tailleBloc) {
        final int from = numeroBloc * tailleBloc;
        final int to = Math.min(from + tailleBloc, donnees.length); // Assure de ne pas dépasser les limites
        return Arrays.copyOfRange(donnees, from, to);
    }

    public Son(final String nomFichier) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(nomFichier));
            AudioFormat af = ais.getFormat();

            if (af.getChannels() == 1 && af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && af.getSampleSizeInBits() == 16) {
                final int nombreDonnees = ais.available();
                final byte[] bufferOctets = new byte[nombreDonnees];
                ais.read(bufferOctets);
                ais.close();
                ByteBuffer bb = ByteBuffer.wrap(bufferOctets);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                ShortBuffer donneesAudio = bb.asShortBuffer();
                donnees = new float[donneesAudio.capacity()];
                for (int i = 0; i < donnees.length; ++i)
                    donnees[i] = (float) donneesAudio.get(i);
                frequence = (int) af.getSampleRate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
