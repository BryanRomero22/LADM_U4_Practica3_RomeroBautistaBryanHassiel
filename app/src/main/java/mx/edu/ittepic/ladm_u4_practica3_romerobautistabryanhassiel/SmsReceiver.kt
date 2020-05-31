package mx.edu.ittepic.ladm_u4_practica3_romerobautistabryanhassiel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SmsMessage
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore

class SmsReceiver : BroadcastReceiver() {

    object Mensajes{
        const val ERRORSINTAXIS = "ERROR 1"
        const val ERRORNUMERO = "ERROR 2"
        const val ERRORTIPO = "ERROR 3"
        const val DOCTOR = "DOCTOR"
        const val DENTISTA = "DENTISTA"
        const val CONSULTA = "CONSULTA"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras
        extras?.let { e ->
            val sms = e.get("pdus") as Array<*>
            for (index in sms.indices) {
                val formato = e.getString("format")
                val sms = SmsMessage.createFromPdu(sms[index] as ByteArray, formato)
                analisarSms(sms.originatingAddress.toString(), sms.messageBody.toString())
            }
        }
    }

    private fun analisarSms(telefono: String, sms: String) {
        val partesSms = sms.split(" ") as ArrayList<String>
        if (!sms.contains(" ")) {
            enviarSms(telefono, Mensajes.ERRORSINTAXIS)
            return
        }
        if (partesSms.size != 3) {
            enviarSms(telefono, Mensajes.ERRORSINTAXIS)
            return
        }
        if (partesSms[0] != Mensajes.CONSULTA) {
            enviarSms(telefono, Mensajes.ERRORSINTAXIS)
            return
        }
        obtenerDatos(telefono, partesSms[1], partesSms[2])
    }

    private fun obtenerDatos(telefono : String, numero : String, tipo : String) {
        var bd = FirebaseFirestore.getInstance()

        when(tipo) {
            Mensajes.DOCTOR -> {
                bd.collection("pacientes").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    var fechaCita = ""
                    for (document in querySnapshot!!){
                        if (document.id.equals(numero)){
                            fechaCita = document.getString("fechaCita").toString()
                        }
                    }
                    val message = "Usted tiene una cita con el Doctor el ${fechaCita}"
                    enviarSms(telefono, message)
                }
            }
            Mensajes.DENTISTA -> {
                bd.collection("pacientes").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    var fechaCita = ""
                    for (document in querySnapshot!!){
                        if (document.id.equals(numero)){
                            fechaCita = document.getString("fechaCita").toString()
                        }
                    }
                    val message = "Usted tiene una cita con el Dentista el ${fechaCita}"
                    enviarSms(telefono, message)
                }
            }
            else -> enviarSms(telefono, Mensajes.ERRORTIPO)
        }
    }

    private fun enviarSms(telefono : String, sms : String) {
        when(sms) {
            Mensajes.ERRORSINTAXIS -> {
                val messageError = "ERROR DE SINTAXIS\nRecuerde que la sintaxis correcta es:\nCONSULTA NUMERO TIPO"
                SmsManager.getDefault().sendTextMessage(telefono, null, messageError, null, null)
            }
            Mensajes.ERRORNUMERO -> {
                val messageError = "ERROR DE SINTAXIS FALTA ESPECIFICAR NUMERO\nRecuerde que la sintaxis correcta es:\nCONSULTA NUMERO TIPO"

                SmsManager.getDefault().sendTextMessage(telefono, null, messageError, null, null)
            }
            Mensajes.ERRORTIPO -> {
                val messageError = "ERROR DE SINTAXIS FALTA ESPECIFICAR TIPO\nRecuerde que la sintaxis correcta es:\nCONSULTA NUMERO TIPO"

                SmsManager.getDefault().sendTextMessage(telefono, null, messageError, null, null)
            }
            else -> {
                SmsManager.getDefault().sendTextMessage(telefono, null, sms, null, null)
            }
        }
    }
}