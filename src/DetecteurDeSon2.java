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
        Son[] allSons = initFichierSon();
        initialiserNeurones(allSons.length-1, TAILLE_FFT);
        final int[] nbBloc = new int[allSons.length];
        listeFFTGuess = new Complexe[allSons.length][][];
        fftForAllSons(allSons, nbBloc);
        listeTrain = new Complexe[allSons.length][][];
        listeTest = new Complexe[allSons.length][][];

        for (int i = 0; i < allSons.length; i++) {
            int nbBlocsTrain = (int) (nbBloc[i] * 0.8);
            int nbBlocsTest = nbBloc[i] - nbBlocsTrain;
            listeTrain[i] = Arrays.copyOfRange(listeFFTGuess[i], 0, nbBlocsTrain);
            listeTest[i] = Arrays.copyOfRange(listeFFTGuess[i], nbBlocsTrain, nbBlocsTrain + nbBlocsTest);
        }

        float moyenne = 0;
        for (int tour = 0; tour < 50; tour++) {
            Complexe[][] verifNeuroneC = entrainerEtPredirePour1Neurone(0, 1, 0);

            System.out.println("Lecture du fichier WAV " + args[0]);
            Son sonToPredict = new Son(args[0]);
            int nbBlocPredict = sonToPredict.donnees().length / TAILLE_FFT;
            Complexe[][] signalToPredict = fftSur1Son(sonToPredict, nbBlocPredict);
            moyenne += prediction(signalToPredict, neurones[0], nbBlocPredict);
        }
        System.out.println("Fin de statistique sur " + 50 + " tours avec " + moyenne / 50 + " de précision pour " + args[0]);
        //entrainerTousLesNeurones();
        
        if (args.length > 0) {
            predireSurFichier(args[0]);
        }


        //testerNeuroneSurTousLesSons(neurones[0],allSons);
    }

    // InitNeurone
    private static void initialiserNeurones(int nombreDeNeurones, int tailleDesEntrees) {
        neurones = new iNeurone[nombreDeNeurones];
        for (int i = 0; i < nombreDeNeurones; i++) {
            neurones[i] = new NeuroneHeaviside(tailleDesEntrees);
        }
    }

    private static Complexe[][] entrainerEtPredirePour1Neurone(int indexTrain1, int indexTrain2,int indexNeurone) {
        Complexe[][] entreeNeuroneC = fusion(listeTrain[indexTrain1], listeTrain[indexTrain2]);
        Complexe[][] verifNeuroneC = fusion(listeTest[indexTrain1], listeTest[indexTrain2]);

        float[][] entreeNeuroneF = convFFTtoEntree(entreeNeuroneC, entreeNeuroneC.length, TAILLE_FFT);
        entreeNeuroneF = normaliserDonnees(entreeNeuroneF);
        float[] resultat = creerTableau(entreeNeuroneF.length);

        System.out.println(resultat.length);
        System.out.println(entreeNeuroneF.length);
        System.out.println("Nombre de tours : " + neurones[0].apprentissage(entreeNeuroneF, resultat));

        //lireSynapseEtBiais();

        prediction(verifNeuroneC, neurones[indexNeurone], verifNeuroneC.length);
        return verifNeuroneC;
    }

    private static void entrainerTousLesNeurones() {
        for (int i = 0; i < neurones.length; i++) {
            int indexTrain1 = i; 
            int indexTrain2 = 5; // harcode
            System.out.println("Entraînement du neurone " + i + " avec les indices " + indexTrain1 + " et " + indexTrain2);
            entrainerEtPredirePour1Neurone(indexTrain1, indexTrain2, i);
        }
    }

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
            "./Sources sonores/Combinaison.wav",
            "./Sources sonores/Bruit.wav"
        };

        Son[] sons = new Son[cheminsFichiers.length];
        for (int i = 0; i < cheminsFichiers.length; i++) {
            sons[i] = new Son(cheminsFichiers[i]);
        }

        return sons;
    }


    private static void predireSurFichier(String nomFichier) {
        System.out.println("Lecture du fichier WAV " + nomFichier);
        Son sonToPredict = new Son(nomFichier);
        int nbBlocPredict = sonToPredict.donnees().length / TAILLE_FFT;
        Complexe[][] signalToPredict = fftSur1Son(sonToPredict, nbBlocPredict);
        prediction(signalToPredict, neurones[0], nbBlocPredict);
    }

    private static Complexe[][] fftSur1Son(Son son, int nbBloc) {
        int tailleBloc = TAILLE_FFT;
        Complexe[][] fft = new Complexe[nbBloc][tailleBloc];

        for (int i = 0; i < nbBloc; i++) {
            float[] bloc = son.bloc_deTaille(i, tailleBloc);
            fft[i] = fftBloc(bloc);
        }

        return fft;
    }

    private static Complexe[] fftBloc(float[] bloc) {
        Complexe[] signalComplexe = new Complexe[bloc.length];
        for (int i = 0; i < bloc.length; i++) {
            signalComplexe[i] = new ComplexeCartesien(bloc[i], 0);
        }

        return FFTCplx.appliqueSur(signalComplexe);
    }

    private static void fftForAllSons(Son[] allSons, int[] nbBloc) {
        for (int i = 0; i < allSons.length; i++) {
            nbBloc[i] = allSons[i].donnees().length / TAILLE_FFT;
            listeFFTGuess[i] = fftSur1Son(allSons[i], nbBloc[i]);
        }
    }

    public static Complexe[][] lireTousLesXBlocs(int tailleBloc, float[] donnees, double pourcentage) {
        int tailleSignal = donnees.length;
        int nombreTotalBlocs = (int) (tailleSignal / tailleBloc * pourcentage); // Calculer le nombre de blocs à extraire
        Complexe[][] tousLesBlocs = new Complexe[nombreTotalBlocs][]; // Initialiser un tableau pour stocker tous les blocs de Complexe

        // Parcourir tous les blocs
        for (int i = 0; i < nombreTotalBlocs; i++) {
            float[] bloc = bloc_deTaille(i, tailleBloc, donnees); // Lire chaque bloc
            tousLesBlocs[i] = convertirEnComplexe(bloc); // Convertir chaque bloc en Complexe et l'ajouter au tableau
        }

        return tousLesBlocs; // Retourner tous les blocs de Complexe
    }

    public static float[] bloc_deTaille(final int numeroBloc, final int tailleBloc, float[] donnees) {
        final int from = numeroBloc * tailleBloc;
        final int to = from + tailleBloc;
        return Arrays.copyOfRange(donnees, from, to);
    }

    public static Complexe[] convertirEnComplexe(float[] bloc) {
        Complexe[] signalComplexe = new Complexe[bloc.length];
        for (int i = 0; i < bloc.length; i++) {
            signalComplexe[i] = new ComplexeCartesien(bloc[i], 0);
        }
        return signalComplexe;
    }

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

    public static float[] creerTableau(int taille) {
        float[] tableau = new float[taille];
        int moitie = taille / 2;

        // Remplir la première moitié de 1.0f
        for (int i = 0; i < moitie; i++) {
            tableau[i] = 1.0f;
        }

        // Remplir la deuxième moitié de 0.0f
        for (int i = moitie; i < taille; i++) {
            tableau[i] = 0.0f;
        }

        return tableau;
    }

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
