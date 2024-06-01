#!/usr/bin/env python3

import wave
from matplotlib.pyplot import *
import sys

TailleFFT = 4096

if len(sys.argv) == 2:
	sonWAV = wave.open(sys.argv[1],'r')
	signal = [int.from_bytes(sonWAV.readframes(1), byteorder='little', signed=True) for echantillon in range(sonWAV.getnframes())]
	specgram(signal, NFFT=TailleFFT, Fs=sonWAV.getframerate(), cmap=cm.gist_heat)
	show()
else:
	print("Veuillez fournir en paramètre un nom de fichier WAV à traiter.")