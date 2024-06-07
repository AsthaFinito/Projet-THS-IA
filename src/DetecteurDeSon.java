import java.util.Arrays;
import java.util.Random;
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



public class DetecteurDeSon {
    private static iNeurone[] neurones;
    private static int TailleCalcul=512; //Taille de la FFT
    private static Complexe[][][] fftResults;
    public static void main(String[] args) {

        if (args.length == 1) {
            // Initialiser les fichiers et les neurones
            String[][] fichiers = initFichiers();
            initNeurones(fichiers.length);
            initialiserNeurones(fichiers.length, TailleCalcul);
            // Entraîner les neurones
          
            entrainerNeurones(fichiers);
            System.out.println("Fin apprentissage");
            float[] donnees=new float[1];
            
            //lireSynapseEtBiais(); //Voir l'état des poids et du biais
            
            afficherSortiesNeuronesAvecTest(args[0]);

            //calculerSortieNeurones()
            
        } else {
            System.out.println("Veuillez donner le nom d'un fichier WAV en paramètre SVP.");
        }
    }

    public static float[][] lireTousLesXBlocs(int tailleBloc, float[] donnees, double pourcentage) {
        int tailleSignal = donnees.length;
        int nombreTotalBlocs = (int) (tailleSignal / tailleBloc * pourcentage); // Calculer le nombre de blocs à extraire
        float[][] tousLesBlocs = new float[nombreTotalBlocs][tailleBloc]; // Initialiser un tableau pour stocker tous les blocs
    
        // Parcourir tous les blocs
        for (int i = 0; i < nombreTotalBlocs; i++) {
            tousLesBlocs[i] = bloc_deTaille(i, tailleBloc, donnees); // Lire chaque bloc et l'ajouter au tableau
            //System.out.println("Lecture d'un bloc");
        }
    
        return tousLesBlocs; // Retourner tous les blocs
    }
    

    //Initialisation des neurones avant entrainement
    private static void initialiserNeurones(int nombreDeNeurones, int tailleDesEntrees) {
        neurones = new iNeurone[nombreDeNeurones];
        for (int i = 0; i < nombreDeNeurones; i++) {
            neurones[i] = new NeuroneHeaviside(tailleDesEntrees);
        }
    }
    //Genere un tableau de 1
    private static float[] genererTableau1(int taille) {
        float[] tableau = new float[taille];
        for (int i = 0; i < taille; i++) {
            tableau[i] =1;
        }
        return tableau;
    }
    //genere un tableau de 0
    private static float[] genererTableau0(int taille) {
        float[] tableau = new float[taille];
        for (int i = 0; i < taille; i++) {
            tableau[i] =0;
        }
        return tableau;
    }
    //Initialisation des fichiers d'entrainements
    private static String[][] initFichiers() {
        return new String[][] {
            {"./Sources sonores/Carre.wav"},
            {"./Sources sonores/Sinusoide.wav"},
            
            {"./Sources sonores/Sinusoide3Harmoniques.wav"},
            {"./Sources sonores/Sinusoide2.wav"},
           
            //{"./Sources sonores/Combinaison.wav"}
        };
    }
    //Resume les poids et le biais
    private static void lireSynapseEtBiais() {
        for (int i = 0; i < neurones.length; i++) {
            if (neurones[i] instanceof Neurone) {
                Neurone neurone = (Neurone) neurones[i];
                System.out.print("Neurone " + i + " - Synapses : ");
                for (float f : neurone.synapses())
                    System.out.print(f + " ");
                System.out.print("\nBiais : ");
                System.out.println(neurone.biais());
            }
        }
    }
    //Initialisation du tableau de neurone
    private static void initNeurones(int length) {
        neurones = new iNeurone[length];
    }
    
    
    
        private static void entrainerNeurones(String[][] fichiers) {
            for(int i = 0; i < neurones.length; i++) { // Pour chaque neurone
                    //System.out.println("Entrainement pour le neurone "+i+" avec comme signal "+fichiers[i][0]);
                    Son son = lireFichierWAV(fichiers[i][0]); // Lire le signal
                    
                    int nombreBlocs = son.donnees().length / TailleCalcul; // Calculer le nombre total de blocs
                    //int nombreBlocsEntrainement = (int) (0.8 * nombreBlocs); // 80% des blocs pour l'entraînement
                    //int nombreBlocsAutres = nombreBlocs - nombreBlocsEntrainement; // Nombre de blocs pour les autres signaux
                    
                    float[][] entreesEntrainement = lireTousLesXBlocs(TailleCalcul, son.donnees(), 0.80);// Tableau d'entrées pour l'entraînement
                    // Tableau de résultats pour l'entraînement
                    

                    float[][] entreesAutres = lireTousLesXBlocs(TailleCalcul, lireFichierWAV( "./Sources sonores/Bruit.wav").donnees(), 0.80); // Tableau d'entrées pour les autres signaux
                    // Tableau de résultats pour les autres signaux
                    
                    // Remplir les tableaux d'entrées et de résultats pour l'entraînement
                    
                    
                    // Fusionner les tableaux d'entrées et de résultats
                    float[][] entrees = fusionner2(entreesEntrainement, entreesAutres);
                    entrees=normaliserDonnees(entrees);
                    float[] resultatsEntrainement =creerTableau(entrees.length);
                    System.out.println("Taille intermeidaire entree : "+entreesEntrainement[0].length+" "+entreesAutres[0].length);
                   // System.out.println("Taille intermeidaire sortie : "+resultatsEntrainement.length+" "+resultatsAutres.length);
                   // System.out.println("Taille de fin : "+entrees[0].length+" "+resultats.length);
                    //System.out.println("Tableau de resultats : " + Arrays.toString(resultats));

                    // // Entraîner le neurone avec les données
                    System.out.println("Nombre de tours : " + neurones[i].apprentissage(entrees, resultatsEntrainement));
                    // break;
                
            }
        }
    
    // Fonction pour fusionner deux tableaux de float
    private static float[] fusionner(float[] tableau1, float[] tableau2) {
        float[] fusion = new float[tableau1.length + tableau2.length];
        System.arraycopy(tableau1, 0, fusion, 0, tableau1.length);
        System.arraycopy(tableau2, 0, fusion, tableau1.length, tableau2.length);
        return fusion;
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
    // Fonction pour fusionner deux tableaux de float[] en un tableau 2D
    public static Complexe[][] fusionner2(Complexe[][] matrice1, Complexe[][] matrice2) {
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
    public static float[][] fusionner2(float[][] matrice1, float[][] matrice2) {
        int totalRows = matrice1.length + matrice2.length;
        int cols = Math.max(matrice1[0].length, matrice2[0].length);
        float[][] fusion = new float[totalRows][cols];
    
        for (int i = 0; i < matrice1.length; i++) {
            fusion[i] = new float[cols];
            System.arraycopy(matrice1[i], 0, fusion[i], 0, matrice1[i].length);
        }
    
        for (int i = 0; i < matrice2.length; i++) {
            fusion[matrice1.length + i] = new float[cols];
            System.arraycopy(matrice2[i], 0, fusion[matrice1.length + i], 0, matrice2[i].length);
        }
    
        return fusion;
    }
    
    //Retourne une classe son pour lire wav
    private static Son lireFichierWAV(String fichier) {
        return new Son(fichier);
    }
    //Faire la fft
    
    private static Complexe[] appliquerFFT2(float[] bloc) {
        // Conversion du bloc en nombres complexes
        Complexe[] signalComplexe = new Complexe[bloc.length];
        for (int i = 0; i < bloc.length; i++) {
            signalComplexe[i] = new ComplexeCartesien(bloc[i], 0);
        }
    
        // Application de la FFT sur le bloc de données
        Complexe[] resultat = FFTCplx.appliqueSur(signalComplexe);
    
        // Analyse des résultats de la FFT
        //System.out.println("Résultats de la FFT :");
        for (int i = 0; i < resultat.length; i++) {
            //System.out.print(i + " : (" + (float) resultat[i].reel() + " ; " + (float) resultat[i].imag() + "i)");
            //System.out.println(", (" + (float) resultat[i].mod() + " ; " + (float) resultat[i].arg() + " rad)");
        }
        // System.out.println("FFT FINI");
        return resultat;
    }
    //Extrait seulement le module et la phase de nos données pour anaylse neurone
    private static float[] extraireCaracteristiques(Complexe[] fftResult) {
        float[] features = new float[fftResult.length];
        for (int i = 0; i < fftResult.length; i++) {
            features[i] = (float) fftResult[i].mod();
        }
        return features;
    }
    
    //Duplicate fonction pour la prédiction (pour l'instant useless)
    public static float[] extraireCaracteristiquesPrediction(Complexe[] fftResult) {
        int n = fftResult.length; // Nombre de valeurs dans le résultat de la FFT
        float[] amplitudes = new float[n]; // Initialisation du tableau d'amplitudes
    
        // Extraction des amplitudes
        for (int i = 0; i < n; i++) {
            amplitudes[i] = (float) fftResult[i].mod(); // Amplitude
        }
    
        return amplitudes;
    }
    
    
    private static void afficherSortiesNeuronesAvecTest(String fichierTest) {
        Son sonTest = lireFichierWAV(fichierTest); // Charger le son à tester
        float[][] blocsTest = lireTousLesXBlocs(TailleCalcul, sonTest.donnees(),1.0); // Lire tous les blocs du son
    
        for (int i = 0; i < neurones.length; i++) {
            System.out.println("Neurone " + i + ":");
    
            float sommeSorties = 0; // Variable pour accumuler les sorties de chaque bloc
    
            for (int j = 0; j < blocsTest.length; j++) {
                Complexe[] fftResult = appliquerFFT2(blocsTest[j]); // Appliquer la FFT sur le bloc
                float[] entree = extraireCaracteristiques(fftResult); // Extraire les caractéristiques FFT
    
                // Calculer la sortie du neurone pour chaque bloc et l'ajouter à la somme
                neurones[i].metAJour(entree);
                float sortie = neurones[i].sortie();
                sommeSorties += sortie;
    
                //System.out.println("   Sortie pour bloc " + j + " : " + sortie);
            }
    
            // Calculer la moyenne des sorties et l'afficher
            float moyenneSorties = sommeSorties / blocsTest.length;
            System.out.println("Moyenne des sorties pour le neurone " + i + " : " + moyenneSorties);
        }
    }
    
    
    
    
    
    //On normalise les données pour facilier l'entrainement
    // On normalise seulement les amplitudes des données pour faciliter l'entrainement
// On normalise seulement les amplitudes des données pour faciliter l'entrainement
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
public static float[] bloc_deTaille(final int numeroBloc, final int tailleBloc,float[] donnees) {
    final int from = numeroBloc * tailleBloc;
    final int to = from + tailleBloc;
    return Arrays.copyOfRange(donnees, from, to);
}




    
    
}

class Son {
    private int frequence;
    private float[] donnees;

    public int frequence() { return frequence; }
    public float[] donnees() { return donnees; }
    public float[] bloc_deTaille(final int numeroBloc, final int tailleBloc) {
        final int from = numeroBloc * tailleBloc;
        final int to = from + tailleBloc;
        return Arrays.copyOfRange(donnees, from, to);
    }

    public Son(final String nomFichier) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(nomFichier));
            AudioFormat af = ais.getFormat();

            if (af.getChannels() == 1 && 
                af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && 
                af.getSampleSizeInBits() == 16) {
                final int NombreDonnees = ais.available();
                final byte[] bufferOctets = new byte[NombreDonnees];
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


