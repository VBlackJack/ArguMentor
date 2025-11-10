package com.argumentor.app.data.constants

/**
 * Catalog of logical fallacies and sophisms.
 * Used for tagging rebuttals and educational purposes.
 */
object FallacyCatalog {
    data class Fallacy(
        val id: String,
        val name: String,
        val description: String,
        val example: String
    )

    val FALLACIES = listOf(
        Fallacy(
            id = "ad_hominem",
            name = "Ad Hominem",
            description = "Attaquer la personne plutôt que son argument",
            example = "Tu ne peux pas avoir raison, tu es trop jeune"
        ),
        Fallacy(
            id = "straw_man",
            name = "Épouvantail (Straw Man)",
            description = "Déformer l'argument de l'adversaire pour le réfuter plus facilement",
            example = "Tu veux réguler les armes ? Donc tu veux désarmer tout le monde !"
        ),
        Fallacy(
            id = "appeal_to_ignorance",
            name = "Appel à l'ignorance",
            description = "Affirmer qu'une proposition est vraie car elle n'a pas été prouvée fausse",
            example = "Personne n'a prouvé que Dieu n'existe pas, donc il existe"
        ),
        Fallacy(
            id = "post_hoc",
            name = "Post Hoc Ergo Propter Hoc",
            description = "Confondre corrélation et causalité",
            example = "J'ai porté ce chapeau et j'ai gagné, donc ce chapeau porte chance"
        ),
        Fallacy(
            id = "false_dilemma",
            name = "Faux Dilemme",
            description = "Présenter seulement deux options alors qu'il en existe d'autres",
            example = "Soit tu es avec nous, soit tu es contre nous"
        ),
        Fallacy(
            id = "begging_question",
            name = "Pétition de Principe",
            description = "Présupposer ce qu'on cherche à démontrer",
            example = "La Bible est vraie car elle est la parole de Dieu"
        ),
        Fallacy(
            id = "slippery_slope",
            name = "Pente Glissante",
            description = "Affirmer qu'une action entraînera inévitablement des conséquences extrêmes",
            example = "Si on autorise le mariage gay, bientôt on autorisera tout"
        ),
        Fallacy(
            id = "postdiction",
            name = "Postdiction",
            description = "Présenter une prédiction écrite après les faits comme prophétique",
            example = "Ce texte a prédit la chute de Babylone (écrit après l'événement)"
        ),
        Fallacy(
            id = "cherry_picking",
            name = "Cherry Picking",
            description = "Sélectionner uniquement les données qui soutiennent sa thèse",
            example = "Regardez cette étude qui prouve mon point (en ignorant 20 autres)"
        ),
        Fallacy(
            id = "appeal_to_tradition",
            name = "Appel à la Tradition",
            description = "Affirmer qu'une chose est vraie car elle a toujours été ainsi",
            example = "On a toujours fait comme ça, donc c'est la bonne façon"
        ),
        Fallacy(
            id = "appeal_to_authority",
            name = "Appel à l'Autorité",
            description = "Invoquer une autorité hors de son domaine d'expertise",
            example = "Einstein croyait en Dieu, donc Dieu existe"
        ),
        Fallacy(
            id = "appeal_to_popularity",
            name = "Appel à la Popularité",
            description = "Affirmer qu'une chose est vraie car beaucoup y croient",
            example = "Des millions de gens croient cela, ça ne peut pas être faux"
        ),
        Fallacy(
            id = "circular_reasoning",
            name = "Raisonnement Circulaire",
            description = "Utiliser la conclusion comme prémisse",
            example = "C'est vrai parce que c'est vrai"
        ),
        Fallacy(
            id = "tu_quoque",
            name = "Tu Quoque",
            description = "Rejeter un argument en accusant l'autre d'hypocrisie",
            example = "Tu me dis d'arrêter de fumer mais tu fumes aussi"
        ),
        Fallacy(
            id = "hasty_generalization",
            name = "Généralisation Hâtive",
            description = "Tirer une conclusion générale à partir d'exemples insuffisants",
            example = "J'ai rencontré deux Français arrogants, donc tous les Français sont arrogants"
        ),
        Fallacy(
            id = "red_herring",
            name = "Diversion (Red Herring)",
            description = "Détourner l'attention du sujet principal par un argument non pertinent",
            example = "Pourquoi parler de la pollution ? Il y a des gens qui meurent de faim !"
        ),
        Fallacy(
            id = "no_true_scotsman",
            name = "Aucun Vrai Écossais",
            description = "Modifier la définition pour exclure les contre-exemples gênants",
            example = "Aucun vrai chrétien ne ferait cela. - Mais Paul l'a fait ! - Alors Paul n'était pas un vrai chrétien."
        ),
        Fallacy(
            id = "loaded_question",
            name = "Question Piège",
            description = "Poser une question contenant une présupposition problématique",
            example = "Avez-vous arrêté de mentir ?"
        ),
        Fallacy(
            id = "appeal_to_emotion",
            name = "Appel à l'Émotion",
            description = "Manipuler les émotions plutôt que présenter des arguments rationnels",
            example = "Comment pouvez-vous être contre cette loi ? Pensez aux enfants !"
        ),
        Fallacy(
            id = "appeal_to_nature",
            name = "Appel à la Nature",
            description = "Affirmer qu'une chose est bonne parce qu'elle est naturelle",
            example = "Les remèdes naturels sont meilleurs car ils sont naturels"
        ),
        Fallacy(
            id = "false_equivalence",
            name = "Fausse Équivalence",
            description = "Comparer deux choses incomparables comme si elles étaient équivalentes",
            example = "Les scientifiques ont déjà eu tort, donc la science vaut autant que mon opinion"
        ),
        Fallacy(
            id = "burden_of_proof",
            name = "Renversement de la Charge de la Preuve",
            description = "Exiger que l'adversaire prouve que l'affirmation est fausse",
            example = "Prouvez-moi que les fantômes n'existent pas !"
        ),
        Fallacy(
            id = "texas_sharpshooter",
            name = "Tireur d'Élite Texan",
            description = "Choisir sélectivement les données pour créer un motif illusoire",
            example = "Regardez toutes ces coïncidences dans cette prophétie (en ignorant les erreurs)"
        ),
        Fallacy(
            id = "middle_ground",
            name = "Juste Milieu",
            description = "Affirmer que le compromis entre deux positions est nécessairement correct",
            example = "La Terre est ronde vs plate ? Disons qu'elle est légèrement courbée."
        ),
        Fallacy(
            id = "anecdotal",
            name = "Preuve Anecdotique",
            description = "Utiliser une expérience personnelle comme preuve générale",
            example = "Mon grand-père fumait et a vécu 90 ans, donc le tabac n'est pas dangereux"
        ),
        Fallacy(
            id = "composition",
            name = "Sophisme de Composition",
            description = "Affirmer que ce qui est vrai d'une partie est vrai du tout",
            example = "Chaque joueur est excellent, donc l'équipe sera excellente"
        ),
        Fallacy(
            id = "division",
            name = "Sophisme de Division",
            description = "Affirmer que ce qui est vrai du tout est vrai de chaque partie",
            example = "L'équipe est riche, donc chaque joueur doit être riche"
        ),
        Fallacy(
            id = "genetic_fallacy",
            name = "Sophisme Génétique",
            description = "Juger quelque chose uniquement par son origine",
            example = "Cette théorie vient de Nazi, donc elle est fausse"
        ),
        Fallacy(
            id = "bandwagon",
            name = "Effet de Mode",
            description = "Affirmer qu'une chose est correcte parce que tout le monde le fait",
            example = "Tout le monde utilise ce produit, tu devrais aussi"
        )
    )

    fun getFallacyById(id: String): Fallacy? = FALLACIES.find { it.id == id }

    fun searchFallacies(query: String): List<Fallacy> {
        val lowerQuery = query.lowercase()
        return FALLACIES.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
        }
    }
}
