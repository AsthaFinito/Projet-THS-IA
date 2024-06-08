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



public class DetecteurDeSon3 {
    final static int tailleBloc = 512;

    public static void main(String[] args) {
        System.out.println("Lecture du fichier WAV " + args[0]);
        Son2 sonToPredict = new Son2(args[0]);
        int nbBlocPredict = sonToPredict.donnees().length / tailleBloc - 1;

        // Le son à prédire
        Complexe[][] signalToPredict = appliqueFFT(sonToPredict, nbBlocPredict);

        // Tous les sons d'entrainement
        Son2 listeSonBase[] = recupSonsEntrainement();

        // Le nombre de sons d'entrainement
        int nbSonBase = listeSonBase.length;

        // Correspond à notre base de signaux d'entrainement en FFT
        Complexe[][][] listeFFTBase = new Complexe[nbSonBase][][];

        // Nombre de blocs par signal
        // final int nbBloc[] = new int[nbSonBase];
        final int nbBloc[] = new int[tailleBloc];

        // On applique la FFT sur chaque signal
        for (int i = 0; i < nbSonBase; i++) {
            System.out.printf("Application de la FFT sur le son d'entraînement %d, blocs : %d\n", i, nbBloc.length);
            nbBloc[i] = listeSonBase[i].donnees().length / tailleBloc - 1;
            listeFFTBase[i] = appliqueFFT(listeSonBase[i], nbBloc[i]);
        }

        // Notre liste de signaux qui servira comme base d'entrainement pour notre
        // modèle
        Complexe[][][] listeTrain = new Complexe[nbSonBase][][];

        // Notre liste de signaux qui servira comme base de tests pour notre modèle
        Complexe[][][] listeTest = new Complexe[nbSonBase][][];

        for (int i = 0; i < nbSonBase; i++) {
            // On récupère 80% de nos signaux pour l'entrainement
            listeTrain[i] = Arrays.copyOfRange(listeFFTBase[i], 0, 80 * nbBloc[i] / 100);
            // On récupère 20% de nos signaux pour nos tests
            listeTest[i] = Arrays.copyOfRange(listeFFTBase[i], 80 * nbBloc[i] / 100, nbBloc[i]);
        }

        // On concatène 2 bases d'entrainement de nos signaux pour faire la base
        // d'entrainement du modèle
        Complexe[][] baseTrain = concatenerMatrices(listeTrain[0], listeTrain[2]);

        // On concatène 2 bases de test de nos signaux pour faire la base de test
        // du modèle
        Complexe[][] baseTest = concatenerMatrices(listeTest[0], listeTest[2]);

        System.out.printf("Concaténation des matrices d'entraînement : %d blocs\n", baseTrain.length);
        System.out.printf("Concaténation des matrices de test : %d blocs\n", baseTest.length);


        // Création du neurone carré
        iNeurone nCarre = new NeuroneHeaviside(tailleBloc);

        // Entrainement du neurone carré
        nCarre = neuroneCarre(baseTrain, nCarre, baseTrain.length);

        // Test de notre modèle
        prediction(baseTest, nCarre, baseTest.length);
        System.out.printf("Prédiction sur le signal de test : %f\n", nCarre.sortie());

        // Prédiction du son entrée par l'utilisateur
        System.out.println("Début de la prédiction sur le signal utilisateur\n");
        prediction(signalToPredict, nCarre, nbBlocPredict);
    }

    private static Complexe[][] appliqueFFT(Son2 son, int nbBloc) {
        Complexe[][] fft = new Complexe[nbBloc][tailleBloc];
        for (int i = 0; i < nbBloc; i++) {
            son.bloc_deTaille(i, tailleBloc);
            fft[i] = analyseSon(son, tailleBloc);
        }
        return fft;
    }

    public static Complexe[][] concatenerMatrices(Complexe[][] matrice1, Complexe[][] matrice2) {
        // Déterminer la taille de la matrice résultante
        int tailleResultante = matrice1.length + matrice2.length;

        // Créer la matrice résultante
        Complexe[][] matriceResultante = new Complexe[tailleResultante][];

        // Copier les éléments de la première matrice dans la matrice résultante
        for (int i = 0; i < matrice1.length; i++) {
            matriceResultante[i] = matrice1[i];
        }

        // Copier les éléments de la deuxième matrice dans la matrice résultante
        for (int i = 0; i < matrice2.length; i++) {
            matriceResultante[i + matrice1.length] = matrice2[i];
        }

        // Retourner la matrice résultante
        return matriceResultante;
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

    private static float[][] normaliserDonnees(float[][] donnees) {
        int n = donnees.length; // Nombre de lignes (nombre de vecteurs)
        int m = donnees[0].length; // Nombre de colonnes (nombre de caractéristiques par vecteur)
        float[][] normalisees = new float[n][m];

        // Trouver le min et le max de l'amplitude
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (donnees[i][j] < min)
                    min = donnees[i][j];
                if (donnees[i][j] > max)
                    max = donnees[i][j];
            }
        }

        float range = max - min;

        // Vérifier si le range est non nul
        if (range != 0) {
            // Normaliser les amplitudes
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    normalisees[i][j] = (donnees[i][j] - min) / range;
                }
            }
        } else {
            // Si le range est nul, réinitialiser toutes les amplitudes à zéro
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    normalisees[i][j] = 0;
                }
            }
        }
        return normalisees;
    }

    public static iNeurone neuroneCarre(Complexe[][] signalTrain, iNeurone nCarre, int nbBloc) {
        // Conversion de notre signal à notre entrée de neurone
        final int taille = signalTrain[0].length;
        float[][] entrees = convFFTtoEntree(signalTrain, nbBloc, taille);

        // Normalisation des valeurs
        entrees = normaliserDonnees(entrees);

        // Résultat attendu pour l'entraînement du signal
        final float[] resultatsCarre = new float[nbBloc];

        // Remplir les 80% premiers avec 1 car se sont les valeurs d'entrainement
        // correct
        Arrays.fill(resultatsCarre, 0, nbBloc / 2, 1);

        // Remplir les 20% restants avec 0 car se sont les valeurs d'entrainement
        // incorrect
        Arrays.fill(resultatsCarre, nbBloc / 2, nbBloc, 0);

        System.out.println("Apprentissage...");

        // On lance l'apprentissage de la fonction carré sur ce neurone
        System.out.println("Nombre de tours : " + nCarre.apprentissage(entrees, resultatsCarre));

        return nCarre;
    }

    public static void prediction(Complexe[][] signalTrain, iNeurone nCarre, int nbBloc) {
        // Conversion de notre signal à notre entrée de neurone
        final int taille = signalTrain[0].length;
        float[][] entrees = convFFTtoEntree(signalTrain, nbBloc, taille);

        // Normalisation des valeurs
        entrees = normaliserDonnees(entrees);

        // On affiche chaque cas appris
        for (int i = 0; i < nbBloc; i++) {
            // Pour une entrée donnée
            final float[] entree = entrees[i];
            // On met à jour la sortie du neurone
            nCarre.metAJour(entree);
            // On affiche cette sortie
            System.out.println("Entree " + i + " : " + nCarre.sortie());
        }
    }

    public static Son2[] recupSonsEntrainement() {
		String[] nomFichiersEntrainement = { "Sources sonores/Carre.wav", "Sources sonores/Bruit.wav",
				"Sources sonores/Sinusoide.wav",
				"Sources sonores/Sinusoide2.wav", "Sources sonores/Sinusoide3Harmoniques.wav" };
		Son2[] listeSonsEntrainement = new Son2[nomFichiersEntrainement.length];
		for (int i = 0; i < nomFichiersEntrainement.length; i++) {
			listeSonsEntrainement[i] = new Son2(nomFichiersEntrainement[i]);
		}
		return listeSonsEntrainement;
	}

    public static Complexe[] analyseSon(Son2 son, int taille) {
		Complexe[] signal = new Complexe[taille];
		for (int i = 0; i < taille; ++i)
			signal[i] = new ComplexeCartesien((double) son.donnees()[i], 0); // pour chaque case on attribue un complexe
		// On applique la FFT sur ce signal
		Complexe[] resultat = FFTCplx.appliqueSur(signal); // on appel la fonction pour faire la TF discrète

		// FFTVisualiseur.afficheFFT(resultat);

		return resultat;
	}

}

class Son2 {
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

    public Son2(final String nomFichier) {
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