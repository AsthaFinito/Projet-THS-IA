import java.util.Arrays;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;


// *****************************************************************************
// La classe Son illustre la lecture de fichiers son de type WAV,
// au format 16 bits monaural non compressé
// Installez et utilisez éventuellement Audacity pour convertir vos
// propres sons dans ce format

public class Son
{
	private int frequence;
	private float[] donnees;

	public int frequence() {return frequence;}
	public float[] donnees() {return donnees;}
	public float[] bloc_deTaille(final int numeroBloc, final int tailleBloc)
	{
		final int from = numeroBloc*tailleBloc;
		final int to = from+tailleBloc;
		return Arrays.copyOfRange(donnees, from, to);
	}

	// Constructeur d'un objet permettant de lire un fichier son mono-canal
	// 16 bits PCM little endian, en utilisant les API Java
	public Son(final String nomFichier)
	{
		try
		{
			// Ouvrir le fichier comme une source audio
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(nomFichier));
			// Obtenir des informations sur cette source
			AudioFormat af = ais.getFormat();

			if (af.getChannels() == 1 &&	// Si le signal est monophonique
				af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED &&	// et qu'il est en Pulse Code Modulation signé
				af.getSampleSizeInBits() == 16)	// et que les échantillons sont sur 16 bits
			{
				final int NombreDonnees = ais.available();	// Combien d'octets constituent les données
				final byte[] bufferOctets = new byte[NombreDonnees];	// Préparer un buffer pour lire tout le flux du fichier
				ais.read(bufferOctets);	// Lire le fichier dans le buffer d'octets
				ais.close();	// On peut fermer le flux du fichier
				ByteBuffer bb = ByteBuffer.wrap(bufferOctets);	// Prépare le travail sur le buffer
				bb.order(ByteOrder.LITTLE_ENDIAN);	// Indique le format des données lues dans le WAV
				ShortBuffer donneesAudio = bb.asShortBuffer();	// Préparer un buffer pour interpréter les données en tableau de short
				donnees = new float[donneesAudio.capacity()];
				for (int i = 0; i < donnees.length; ++i)
					donnees[i] = (float)donneesAudio.get(i);
				// Récupérer la fréquence du fichier audio
				frequence = (int)af.getSampleRate();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main (String[] args)
	{
		if (args.length == 1)
		{
			System.out.println("Lecture du fichier WAV "+args[0]);
			Son son = new Son(args[0]);
			System.out.println("Fichier "+args[0]+" : "+son.donnees().length+" échantillons à "+son.frequence()+"Hz");
			System.out.println("Bloc 1 : "+son.bloc_deTaille(1, 512).length+" échantillons à "+son.frequence()+"Hz");
		}
		else
			System.out.println("Veuillez donner le nom d'un fichier WAV en paramètre SVP.");
	}
}
