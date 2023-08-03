package fr.isen.dupont.mehdidroidburger

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var editTextNom: EditText
    private lateinit var editTextPrenom: EditText
    private lateinit var editTextAdresse: EditText
    private lateinit var editTextNumero: EditText
    private lateinit var spinnerBurger: Spinner
    private lateinit var editTextHeure: EditText
    private lateinit var buttonValider: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        this.setContentView(R.layout.activity_main);


        // Initialize views
        editTextNom = findViewById(R.id.editTextNom)
        editTextPrenom = findViewById(R.id.editTextPrenom)
        editTextAdresse = findViewById(R.id.editTextAdresse)
        editTextNumero = findViewById(R.id.editTextNumero)
        spinnerBurger = findViewById(R.id.spinnerBurger)
        editTextHeure = findViewById(R.id.editTextHeure)
        buttonValider = findViewById(R.id.buttonValider)

        // Set up the burger list in the spinner
        val burgerList = resources.getStringArray(R.array.burger_list).toMutableList()
        val placeholderText = "Sélectionnez un burger"
        burgerList.add(
            0,
            placeholderText
        ) // Ajouter une option avec le texte placeholder en première position
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, burgerList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBurger.adapter = adapter

        // Définir l'option avec le texte placeholder comme la première sélection
        spinnerBurger.setSelection(0)


        // Handle item selection in the spinner
        spinnerBurger.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedBurger = parent?.getItemAtPosition(position).toString()
                if (selectedBurger != placeholderText) {
                    Toast.makeText(
                        this@MainActivity,
                        "Selected burger: $selectedBurger",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }


        // Handle button click event
        buttonValider.setOnClickListener {
            validerFormulaire(null) // Appel de la fonction validerFormulaire avec null comme argument
        }
    }

    fun showTimePickerDialog(v: View?) {
        val calendar: Calendar = Calendar.getInstance()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            this,
            { view, hourOfDay, minute -> // Mettre à jour le champ "heure de livraison" avec l'heure sélectionnée
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                editTextHeure.setText(selectedTime)
            }, hour, minute, DateFormat.is24HourFormat(this)
        )
        timePickerDialog.setOnCancelListener { // Réinitialiser le champ "heure de livraison" si le dialogue est annulé
            editTextHeure.setText("")
        }
        timePickerDialog.show()
    }


    fun validerFormulaire(v: View?) {
        val nom = editTextNom.text.toString()
        val prenom = editTextPrenom.text.toString()
        val adresse = editTextAdresse.text.toString()
        val numero = editTextNumero.text.toString()
        val burger = spinnerBurger.selectedItem.toString()
        val heure = editTextHeure.text.toString()
        val champsManquants = StringBuilder()
        if (nom.isEmpty()) {
            champsManquants.append("\n• Nom")
        }
        if (prenom.isEmpty()) {
            champsManquants.append("\n• Prénom")
        }
        if (adresse.isEmpty()) {
            champsManquants.append("\n• Adresse")
        }
        if (numero.isEmpty()) {
            champsManquants.append("\n• Numéro de téléphone")
        }
        if (burger.isEmpty() || burger == "Sélectionnez un burger") {
            champsManquants.append("\n• Burger")
        }
        if (heure.isEmpty()) {
            champsManquants.append("\n• Heure de livraison")
        }
        if (champsManquants.isNotEmpty()) {
            // Afficher un message d'erreur avec les champs manquants
            afficherMessageErreur("Veuillez remplir les champs suivants : $champsManquants")
            return
        }

        // Créer un objet JSON pour stocker les données de la commande
        val commandeJson = JSONObject()
        try {
            commandeJson.put("lastname", nom)
            commandeJson.put("firstname", prenom)
            commandeJson.put("address", adresse)
            commandeJson.put("phone", numero)
            commandeJson.put("burger", burger)
            commandeJson.put("delivery_time", heure)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // Envoyer l'objet JSON au serveur
        envoyerCommandeAuServeur(commandeJson)
    }


    private fun afficherMessageErreur(message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Erreur")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun envoyerCommandeAuServeur(commandeJson: JSONObject) {
        val url = "http://test.api.catering.bluecodegames.com/user/order"

        val jsonObject = JSONObject()
        jsonObject.put("id_shop", "1")
        jsonObject.put("id_user", 663)
        jsonObject.put(
            "msg",
            "{'firstname':'${commandeJson.getString("firstname")}','lastname':'${
                commandeJson.getString(
                    "lastname"
                )
            }','address':'${commandeJson.getString("address")}','phone':'${commandeJson.getString("phone")}','burger':'${
                commandeJson.getString(
                    "burger"
                )
            }','delivery_time':'${commandeJson.getString("delivery_time")}'}"
        )


        Log.d("JSON Debug", jsonObject.toString())

        val jsonRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject, {
                Log.w("CategoryActivity", "response : $it")

                // Démarrer l'activité de confirmation de commande
                val intent = Intent(this@MainActivity, ConfirmationCommandeActivity::class.java)
                // Passer les données nécessaires à l'activité de confirmation de commande via l'intent
                intent.putExtra("commandeJson", commandeJson.toString())
                startActivity(intent)

                // Réinitialiser les champs du formulaire
                editTextNom.text.clear()
                editTextPrenom.text.clear()
                editTextAdresse.text.clear()
                editTextNumero.text.clear()
                spinnerBurger.setSelection(0) // Réinitialiser le spinner à la première valeur
                editTextHeure.text.clear()

            },
            {
                Log.e("CategoryActivity", "erreur : $it")
                afficherMessageErreur("ERREUR 400")

            })
        Volley.newRequestQueue(this).add(jsonRequest)
    }

}
