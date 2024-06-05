import java.util.Arrays;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;

public class DetecteurDeSon {
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println("Lecture du fichier WAV " + args[0]);
            Son son = new Son(args[0]);
            System.out.println("Fichier " + args[0] + " : " + son.donnees().length + " échantillons à " + son.frequence() + "Hz");

            // Lecture d'un bloc de données
            float[] bloc = son.bloc_deTaille(0, FFTCplx.TailleFFTtest);

            // Conversion du bloc en nombres complexes
            Complexe[] signalComplexe = new Complexe[bloc.length];
            for (int i = 0; i < bloc.length; i++) {
                signalComplexe[i] = new ComplexeCartesien(bloc[i], 0);
            }

            // Application de la FFT sur le bloc de données
            Complexe[] resultat = FFTCplx.appliqueSur(signalComplexe);

            // Analyse des résultats de la FFT
            System.out.println("Résultats de la FFT :");
            for (int i = 0; i < resultat.length; i++) {
                System.out.print(i + " : (" + (float) resultat[i].reel() + " ; " + (float) resultat[i].imag() + "i)");
                System.out.println(", (" + (float) resultat[i].mod() + " ; " + (float) resultat[i].arg() + " rad)");
            }
        } else {
            System.out.println("Veuillez donner le nom d'un fichier WAV en paramètre SVP.");
        }
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

class FFTCplx {
    public final static int TailleFFTtest = 16;
    public final static double Periode = 3;

    private static Complexe[] demiSignal(Complexe[] signal, int depart) {
        Complexe[] sousSignal = new Complexe[signal.length / 2];
        for (int i = 0; i < sousSignal.length; ++i)
            sousSignal[i] = signal[depart + 2 * i];
        return sousSignal;
    }

    public static Complexe[] appliqueSur(Complexe[] signal) {
        Complexe[] trSignal = new Complexe[signal.length];
        if (signal.length == 1)
            trSignal[0] = new ComplexeCartesien(signal[0].reel(), signal[0].imag());
        else {
            final Complexe[] P0 = appliqueSur(demiSignal(signal, 0));
            final Complexe[] P1 = appliqueSur(demiSignal(signal, 1));
            for (int k = 0; k < signal.length / 2; ++k) {
                final ComplexePolaire expo = new ComplexePolaire(1., -2. * Math.PI * k / signal.length);
                final Complexe temp = P0[k];
                trSignal[k] = temp.plus(expo.fois(P1[k]));
                trSignal[k + signal.length / 2] = temp.moins(expo.fois(P1[k]));
            }
        }
        return trSignal;
    }
}

abstract class Complexe {
    public abstract double reel();
    public abstract double imag();
    public abstract double mod();
    public abstract double arg();
    public abstract Complexe plus(Complexe c);
    public abstract Complexe moins(Complexe c);
    public abstract Complexe fois(Complexe c);
}

class ComplexeCartesien extends Complexe {
    private double re;
    private double im;

    public ComplexeCartesien(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double reel() { return re; }
    public double imag() { return im; }
    public double mod() { return Math.sqrt(re * re + im * im); }
    public double arg() { return Math.atan2(im, re); }
    public Complexe plus(Complexe c) { return new ComplexeCartesien(re + c.reel(), im + c.imag()); }
    public Complexe moins(Complexe c) { return new ComplexeCartesien(re - c.reel(), im - c.imag()); }
    public Complexe fois(Complexe c) {
        return new ComplexeCartesien(re * c.reel() - im * c.imag(), re * c.imag() + im * c.reel());
    }
}

class ComplexePolaire extends Complexe {
    private double r;
    private double theta;

    public ComplexePolaire(double r, double theta) {
        this.r = r;
        this.theta = theta;
    }

    public double reel() { return r * Math.cos(theta); }
    public double imag() { return r * Math.sin(theta); }
    public double mod() { return r; }
    public double arg() { return theta; }
    public Complexe plus(Complexe c) {
        return new ComplexeCartesien(reel() + c.reel(), imag() + c.imag());
    }
    public Complexe moins(Complexe c) {
        return new ComplexeCartesien(reel() - c.reel(), imag() - c.imag());
    }
    public Complexe fois(Complexe c) {
        return new ComplexePolaire(r * c.mod(), theta + c.arg());
    }
}
