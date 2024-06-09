import java.util.Random;
import Neurone.*;

public class testNeurone
{
	public static void main(String[] args)
	{
		final int numTests = 100; // Nombre de tours de test pour l'apprentissage
		// Tableau des entrées de la fonction ET (0 = faux, 1 = vrai)
		final float[][] entrees = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
		
		// Tableau des entrées de la fonction OU (0 = faux, 1 = vrai)
		//float[][] entrees= {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
		// Tableau des sorties de la fonction ET
		final float[] resultats = {0, 0, 0, 1};
		//final float[] resultats = {0,1,1,1};
		System.out.println("Initialisation des tableaux");
		// On crée un neurone taillé pour apprendre la fonction ET
		//final iNeurone n = new NeuroneHeaviside(entrees[0].length);
		//System.out.println("Neurone iNeurone ok");
		final iNeurone n = new NeuroneRelu(entrees[0].length);
		//final iNeurone n = new NeuroneReLU(entrees[0].length);
		
		// Ajouter du bruit aux entrées
       

		System.out.println("Apprentissage…");
		// On lance l'apprentissage de la fonction ET sur ce neurone
		System.out.println("Nombre de tours : "+n.apprentissage(entrees, resultats));
		System.out.println("Apprentissage fini");
		// On affiche les valeurs des synapses et du biais

		// Conversion dynamique d'une référence iNeurone vers une référence neurone.
		// Sans cette conversion on ne peut pas accéder à synapses() et biais()
		// à partir de la référence de type iNeurone
		// Cette conversion peut échouer si l'objet derrière la référence iNeurone
		// n'est pas de type neurone, ce qui n'est cependant pas le cas ici
		final Neurone vueNeurone = (Neurone)n;
		System.out.print("Synapses : ");
		for (final float f : vueNeurone.synapses())
			System.out.print(f+" ");
		System.out.print("\nBiais : ");
		System.out.println(vueNeurone.biais());
		
		// On affiche chaque cas appris
		for (int i = 0; i < entrees.length; ++i)
		{
			// Pour une entrée donnée
			final float[] entree = entrees[i];
			// On met à jour la sortie du neurone
			n.metAJour(entree);
			// On affiche cette sortie
			System.out.println("Entree "+i+" : "+n.sortie());
		}
		// Ajouter du bruit aux entrées pour les tests
        float[][] entreesBruit = ajouterBruitAuxEntrees(entrees, 0.5f); // Ajoute un bruit avec écart-type de 0.1
		int correct_guess_neurone=0;
		int repetitions=100;

		float sommeProbas = 0;
        int totalCases = 0;
        // On affiche chaque cas appris avec bruit
		//for(int nb_ite_bruit=0;nb_ite_bruit<repetitions;++nb_ite_bruit){

		
        // for (int i = 0; i < entreesBruit.length; ++i)
        // {
        //     // Pour une entrée donnée bruitée
        //     final float[] entree = entreesBruit[i];
        //     // On met à jour la sortie du neurone
        //     n.metAJour(entree);
		// 	sommeProbas += n.sortie();
		// 	totalCases++;
		// 	if (Math.round(n.sortie()) == resultats[i]) {
		// 		correct_guess_neurone++;
		// 	}
        //     // On affiche cette sortie
        //     System.out.print("Entree bruitée " + i + " : [");
        //     for (int j = 0; j < entree.length; j++) {
        //         System.out.print(entree[j]);
        //         if (j < entree.length - 1) System.out.print(" ");
        //     }
        //     System.out.println("] => Sortie : " + n.sortie());
        // }
		// }
		// //float accuracy = (float) correct_guess_neurone / (repetitions * entrees.length);
        // //System.out.println("Precision moyenne apres " + repetitions + " repetitions : " + accuracy);
		// // Calculer les statistiques
        // float moyenneProbas = sommeProbas / totalCases;
        // //System.out.println("Moyenne des probabilites apres " + repetitions + " repetitions : " + moyenneProbas);
    }
	
	 public static float[][] ajouterBruitAuxEntrees(float[][] entrees, float ecartType)
    {
        Random random = new Random();
        float[][] entreesBruit = new float[entrees.length][entrees[0].length];

        for (int i = 0; i < entrees.length; i++)
        {
            for (int j = 0; j < entrees[i].length; j++)
            {
                // Ajouter un bruit avec moyenne 0 et écart-type donné
                entreesBruit[i][j] = entrees[i][j] + (float)random.nextGaussian() * ecartType;
            }
        }

        return entreesBruit;
    }
}
