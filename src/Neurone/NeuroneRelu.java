package Neurone;



public class NeuroneRelu extends Neurone
{
	// Fonction d'activation d'un neurone ReLU
	protected float activation(final float valeur) {return Math.max(0, valeur);}
	
	// Constructeur
	public NeuroneRelu(final int nbEntrees) {super(nbEntrees);}
}
