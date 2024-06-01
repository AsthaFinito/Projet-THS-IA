public interface iNeurone
{
	// Calcule la valeur de sortie en fonction des entrées, des poids synaptiques,
	// du biais et de la fonction d'activation
	public void metAJour(final float[] entrees);
	
	// Accesseur pour la valeur de sortie/d'activation du neurone
	public float sortie();

	// Fonction d'apprentissage permettant de mettre à jour les valeurs des 
	// poids synaptiques ainsi que du biais en fonction de données supervisées
	public int apprentissage(final float[][] entrees, final float[] resultats);
}
