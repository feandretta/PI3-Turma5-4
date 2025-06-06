package projeto.integrador.utilities

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import projeto.integrador.config.generateAccessToken
import projeto.integrador.data.model.Access
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//essas chamadas de db e auth são horrorosas dps vou criar um função de repositorio pra isso
//função para cadastrar as senhas
suspend fun registerAccess(access: Access): String{
    //ele não irá retornar caso a SENHA, CATEGORIA ou NOME sejam vazios os demais campos são opcionais

    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid ?: "uid"

    access.senha = CryptoManager.encrypt(access.senha.toString())

    return try{
        db.collection("usuarios")
            .document(uid)
            .collection("acessos")
            .add(access).await()

        "Acesso Cadastrado com sucesso"
    }catch (e: Exception) {

        e.printStackTrace()

        Log.e("Registro Acesso", "Erro ao registrar acesso", e)

        "Erro ao registrar acesso"
    }
}
//retorna todos os acessos do usuário como uma lista de objetos do tipo documentSnapshot
//DOCUMENT SNAPSHOT cotem o .ID do documento no firebase e .getData() pras informações de fato
suspend fun getAllAccess(): List<DocumentSnapshot> {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid ?: "uid"

    return suspendCoroutine { continuation ->
        db.collection("usuarios")
            .document(uid)
            .collection("acessos")
            .get()
            .addOnSuccessListener { result ->
                val documentSnapshots = mutableListOf<DocumentSnapshot>()
                for (document in result) {
                    Log.d("Get Acessos", "${document.id} => ${document.data}")
                    documentSnapshots.add(document)
                }
                continuation.resume(documentSnapshots)
            }
            .addOnFailureListener { exception ->
                Log.d("Get Acessos", "Error getting documents: ", exception)
                continuation.resumeWithException(exception)
            }
    }
}
//mesma coisa que um get normal mas esse você passa o id do acesso específico que você quer retornar
// EM STRING PEDRÃO

suspend fun getAccessByUser(idAccess: String): DocumentSnapshot {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid ?: "uid"

    val docRef = db.collection("usuarios").document(uid).collection("acessos").document(idAccess)
    return docRef.get().await()
}

//passa o id e o acesso novo que ele edita
suspend fun alterAccess(idAccess: String, accessAtualizado: Access): Boolean{
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid ?: "uid"

    accessAtualizado.accessToken = generateAccessToken()
    accessAtualizado.senha = CryptoManager.encrypt(accessAtualizado.senha.toString())

    val docRef = db.collection("usuarios").document(uid).collection("acessos").document(idAccess)

    return suspendCoroutine { continuation ->
        docRef.update(mapOf("nome" to accessAtualizado.nome,
                            "categoria" to accessAtualizado.categoria,
                            "parceiro" to accessAtualizado.dominio,
                            "email" to accessAtualizado.email,
                            "senha" to accessAtualizado.senha,
                            "descricao" to accessAtualizado.descricao))
            .addOnSuccessListener { result ->
                Log.d("Update Acesso", "$result")
                continuation.resume(true)
            }
            .addOnFailureListener { exception ->
                Log.d("Update Acesso", "Error updating document: ", exception)
                continuation.resumeWithException(exception)
            }
    }

    return false
}

//passa o id do acesso e ele deleta permanentemente
suspend fun deleteAccess(idAccess: String): Boolean{
    val auth = Firebase.auth
    val db = Firebase.firestore
    val uid = auth.currentUser?.uid ?: "uid"

    val docRef = db.collection("usuarios").document(uid).collection("acessos").document(idAccess)

    return suspendCoroutine { continuation ->
        docRef.delete()
            .addOnSuccessListener { result ->
                Log.d("Delete Acesso", "$result")
                continuation.resume(true)
            }
            .addOnFailureListener { exception ->
                Log.d("Delete Acesso", "Error updating document: ", exception)
                continuation.resumeWithException(exception)
            }
    }

    return false
}

