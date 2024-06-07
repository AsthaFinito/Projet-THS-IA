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
            
            prediction(args[0]);

            //calculerSortieNeurones()
            
        } else {
            System.out.println("Veuillez donner le nom d'un fichier WAV en paramètre SVP.");
        }
    }

    public static float[][] lireTousLesBlocs( int tailleBloc,float[] donnees) {
        int nombreTotalBlocs = donnees.length / tailleBloc; // Calculer le nombre total de blocs
        float[][] tousLesBlocs = new float[nombreTotalBlocs][tailleBloc]; // Initialiser un tableau pour stocker tous les blocs
    
        // Parcourir tous les blocs
        for (int i = 0; i < nombreTotalBlocs; i++) {
            tousLesBlocs[i] = bloc_deTaille(i, tailleBloc,donnees); // Lire chaque bloc et l'ajouter au tableau
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
            {"./Sources sonores/Bruit.wav"},
            {"./Sources sonores/Sinusoide3Harmoniques.wav"},
            {"./Sources sonores/Sinusoide2.wav"},
            {"./Sources sonores/Combinaison.wav"}
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
        // Déterminer le nombre total de blocs pour initialiser le tableau FFT
        int nombreTotalBlocs = 0;
        for (String[] fichier : fichiers) {
            Son son = lireFichierWAV(fichier[0]);
            nombreTotalBlocs += son.donnees().length / TailleCalcul;
        }
        
        fftResults = new Complexe[fichiers.length][nombreTotalBlocs][TailleCalcul]; // Initialiser le tableau FFT
    
        int blocIndex = 0;
        for (int i = 0; i < neurones.length; i++) {
            System.out.println("Entraînement du neurone numéro : " + i);
            float[] resultats;
    
            int fichierIndex = i % fichiers.length;
            Son son = lireFichierWAV(fichiers[fichierIndex][0]);
    
            float[][] tousLesBlocs = lireTousLesBlocs(TailleCalcul, son.donnees());
            for (float[] bloc : tousLesBlocs) {
                Complexe[] fftResult = appliquerFFT2(bloc);
                fftResults[fichierIndex][blocIndex] = fftResult; // Stocker les résultats de la FFT dans le tableau
                blocIndex++;
                
            }
              // Extraire les caractéristiques des résultats FFT pour l'entraînement
              float[][] entrees = new float[tousLesBlocs.length][TailleCalcul];
              for (int j = 0; j < tousLesBlocs.length; j++) {
                  entrees[j] = extraireCaracteristiques(fftResults[fichierIndex][j]);
              }
              if (fichiers[fichierIndex][0].contains("Carre")) {
                resultats = genererTableau1(tousLesBlocs.length);
            } else {
                resultats = genererTableau0(tousLesBlocs.length);
            }
              //resultats = genererTableau1(tousLesBlocs.length); // Résultats attendus pour le neurone en cours d'entraînement
              entrees = normaliserDonnees(entrees); // Normaliser les données
              System.out.println("Nombre de tours : " + neurones[i].apprentissage(entrees, resultats) + " pour le signal " + fichierIndex);
             blocIndex = 0; // Réinitialiser l'index de bloc pour le prochain fichier
        }
    }

    //Retourne une classe son pour lire wav
    private static Son lireFichierWAV(String fichier) {
        return new Son(fichier);
    }
    //Faire la fft
    private static Complexe[] appliquerFFT(Son son) {
        // Lecture d'un bloc de données
        float[] bloc = son.bloc_deTaille(0, TailleCalcul);

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
    
    private static float[] calculerSortieNeurones(float[] entree) {
        float[] sorties = new float[neurones.length]; // Initialisation du tableau de sorties des neurones
    
        // Pour chaque neurone
        for (int i = 0; i < neurones.length; i++) {
            // Calculer la sortie du neurone pour l'entrée donnée
            neurones[i].metAJour(entree);
            sorties[i] = neurones[i].sortie();
        }
    
        return sorties; // Retourner les sorties des neurones
    }
    
    //Fonction useless pour l'instant 
    private static void prediction(String fichier) {
        // Lire le fichier WAV
        Son son = lireFichierWAV(fichier);
        
        // Lire tous les blocs du signal
        float[][] tousLesBlocs = lireTousLesBlocs(TailleCalcul, son.donnees());
        
        // Initialiser un tableau pour accumuler les sorties de chaque neurone
        float[] sommeSorties = new float[neurones.length];
        int nombreBlocs = tousLesBlocs.length;
    
        for (float[] bloc : tousLesBlocs) {
            // Appliquer la FFT sur chaque bloc
            Complexe[] fftResult = appliquerFFT2(bloc);
            
            // Extraire les caractéristiques des résultats FFT pour la prédiction
            float[] features = extraireCaracteristiquesPrediction(fftResult);
            
            // Normaliser les caractéristiques pour la prédiction
            float[] normalizedFeatures = normaliserDonnees(new float[][] {features})[0];
            
            // Mettre à jour chaque neurone avec les caractéristiques normalisées et accumuler les sorties
            for (int i = 0; i < neurones.length; i++) {
                neurones[i].metAJour(normalizedFeatures);
                float prediction = neurones[i].sortie();
                sommeSorties[i] += prediction;
            }
        }
    
        // Calculer et afficher la moyenne des sorties pour chaque neurone
        for (int i = 0; i < neurones.length; i++) {
            float moyenneSortie = sommeSorties[i] / nombreBlocs;
            System.out.println("Moyenne de la sortie du neurone " + i + " : " + moyenneSortie);
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


