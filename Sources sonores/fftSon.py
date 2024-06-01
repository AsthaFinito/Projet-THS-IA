#!/usr/bin/env python3

import wave
from matplotlib.pyplot import *
import sys

TailleFFT = 4096

if len(sys.argv) == 2:
	sonWAV = wave.open(sys.argv[1],'r')
	signal = [int.from_bytes(sonWAV.readframes(1), byteorder='little', signed=True) for echantillon in range(sonWAV.getnframes())]
	# Déterminez la fréquence du signal audio
	freq = sonWAV.getframerate()
    
    # Multiplication freq
	freq *= 0.5
    
    # Modif du signal d'entré comme demandé
	t = np.linspace(0, len(signal) / sonWAV.getframerate(), len(signal))
	signal_sinus = np.sin(2 * np.pi * freq * t)
	specgram(signal, NFFT=TailleFFT, Fs=sonWAV.getframerate(), cmap=cm.gist_heat)
	show()
else:
	print("Veuillez fournir en paramètre un nom de fichier WAV à traiter.")


"""
Explication code pour la fft

	wave -> biblio pour lire les sons .wav 
	TailleFFT -> définit la taille de la fenetre FFT

	syst.argv -> il faut donc passer en paramètre de lancement des arguments , le premier test if verifie si c'est bien de taille 2
	sonWav -> on ouvre le fichier .wav (premier argument attention)
	signal -> on lit les échantillons audio que l'on converti en entier , on stocke ensuite dans signal
	specgram -> on trace le spectrogramme avec matplotlib
"""