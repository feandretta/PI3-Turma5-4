package projeto.integrador.utilities

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

fun validation(
    context: Context,
    email: String,
    senha: String,
    onResult: (Boolean, String) -> Unit
) {
    val auth = Firebase.auth

    auth.signInWithEmailAndPassword(email, senha)
        .addOnSuccessListener { authResult ->
            val user = authResult.user
            if (user != null && user.isEmailVerified) {
                Log.d("FirebaseAuth", "Login feito com sucesso")
                onResult(true, "Login feito com sucesso")
            } else {
                auth.signOut()
                Log.w("FirebaseAuth", "Email não verificado")
                onResult(false, "Por favor, verifique seu e-mail antes de fazer login.")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("FirebaseAuth", "Erro ao logar", exception)

            val errorMessage = when (exception) {
                is FirebaseAuthInvalidUserException -> {
                    "Usuário não encontrado. Verifique o email digitado."
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    "Senha incorreta. Tente novamente."
                }
                is FirebaseAuthException -> {
                    "Erro na autenticação: ${exception.localizedMessage}"
                }
                else -> {
                    "Erro inesperado: ${exception.localizedMessage}"
                }
            }
            //Retorna o erro para o usuário
            onResult(false, errorMessage)
        }
}