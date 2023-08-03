package fr.isen.dupont.mehdidroidburger

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ConfirmationCommandeActivity : AppCompatActivity() {

    private lateinit var listViewDerniereCommande: ListView
    private lateinit var listViewAutresCommandes: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation_commande)

        // Récupérer les références des éléments de votre layout
        listViewDerniereCommande = findViewById(R.id.list_last_order)
        listViewAutresCommandes = findViewById(R.id.liste_autres_commandes)

        // Récupérer les commandes passées de l'utilisateur
        recupererCommandesPassees()
    }

    private fun recupererCommandesPassees() {
        val url = "http://test.api.catering.bluecodegames.com/listorders"
        val idShop = "1"
        val idUser = 663

        val jsonObject = JSONObject()
        jsonObject.put("id_shop", idShop)
        jsonObject.put("id_user", idUser)

        val jsonRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                // Traitement de la réponse réussie du web service
                val commandes = response.getJSONArray("data")

                // Récupérer la dernière commande
                val derniereCommande = commandes.getJSONObject(0)
                val idDerniereCommande = derniereCommande.optString("receiver")
                val dateDerniereCommande = derniereCommande.optString("create_date")
                val messageDerniereCommande = derniereCommande.optString("message")

                // Extraire les informations du champ "message" de la dernière commande
                val messageJson = JSONObject(messageDerniereCommande)
                val firstname = messageJson.optString("firstname")
                val lastname = messageJson.optString("lastname")
                val address = messageJson.optString("address")
                val phone = messageJson.optString("phone")
                val burger = messageJson.optString("burger")
                val deliveryTime = messageJson.optString("delivery_time")

                // Afficher la dernière commande dans une liste distincte
                val listeDerniereCommande = mutableListOf<String>()
                val infoDerniereCommande =
                    "Commande $idDerniereCommande \nDate: $dateDerniereCommande\nNom : $firstname $lastname\nAdresse : $address\nTéléphone : $phone\nBurger : $burger\nHeure de livraison : $deliveryTime"
                listeDerniereCommande.add(infoDerniereCommande)

                // Afficher toutes les autres commandes dans une autre liste
                val listeAutresCommandes = mutableListOf<String>()
                for (i in 1 until commandes.length()) {
                    val commande = commandes.getJSONObject(i)
                    val idCommande = commande.optString("receiver")
                    val dateCommande = commande.optString("create_date")
                    val messageCommande = commande.optString("message")

                    // Extraire les informations du champ "message" de chaque autre commande
                    val messageJsonCommande = JSONObject(messageCommande)
                    val firstnameCommande = messageJsonCommande.optString("firstname")
                    val lastnameCommande = messageJsonCommande.optString("lastname")
                    val addressCommande = messageJsonCommande.optString("address")
                    val phoneCommande = messageJsonCommande.optString("phone")
                    val burgerCommande = messageJsonCommande.optString("burger")
                    val deliveryTimeCommande = messageJsonCommande.optString("delivery_time")

                    val infoCommande =
                        "Commande $idCommande \nDate: $dateCommande\nNom : $firstnameCommande $lastnameCommande\nAdresse : $addressCommande\nTéléphone : $phoneCommande\nBurger : $burgerCommande\nHeure de livraison : $deliveryTimeCommande"
                    listeAutresCommandes.add(infoCommande)
                }

                // Afficher la liste de la dernière commande dans le ListView correspondant
                val adapterDerniereCommande = ArrayAdapter(
                    this@ConfirmationCommandeActivity,
                    android.R.layout.simple_list_item_1,
                    listeDerniereCommande
                )
                listViewDerniereCommande.adapter = adapterDerniereCommande
/*
                // Afficher la liste des autres commandes dans le ListView correspondant
                val adapterAutresCommandes = ArrayAdapter(
                    this@ConfirmationCommandeActivity,
                    android.R.layout.simple_list_item_1,
                    listeAutresCommandes
                )
                listViewAutresCommandes.adapter = adapterAutresCommandes

 */
                // Afficher la liste des commandes passées dans le ListView avec l'adaptateur personnalisé
                val adapterAutresCommande = CommandeListAdapter(this@ConfirmationCommandeActivity, listeAutresCommandes)
                listViewAutresCommandes.adapter = adapterAutresCommande
            },
            { error ->
                // Gérer l'erreur en cas de réponse du web service
                Log.e(
                    "ConfirmationCommande",
                    "Erreur lors de la récupération des commandes : $error"
                )
            }
        )

        // Ajouter la requête à la file d'attente de la bibliothèque Volley pour l'exécuter
        Volley.newRequestQueue(this).add(jsonRequest)


    }

}

class CommandeListAdapter(context: Context, private val commandes: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, commandes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        // Ajouter un espacement après chaque commande sauf la dernière
        if (position < count - 1) {
            textView.setPadding(0, 0, 0, 20) // Espacement de 20 pixels en bas
        } else {
            // Réinitialiser l'espacement pour la dernière commande
            textView.setPadding(0, 0, 0, 0)
        }

        return view
    }
}
