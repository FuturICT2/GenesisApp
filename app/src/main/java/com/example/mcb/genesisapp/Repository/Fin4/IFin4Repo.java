package com.example.mcb.genesisapp.Repository.Fin4;

import android.provider.ContactsContract;

public interface IFin4Repo {



    /*
        User APIs
     */

    /*
        Register
     */
    void postRegister();

    /*
        Login to the Server
     */
    void postLogin();

    /*
        Logout from the Server
     */
    void postLogout();

    /*
        Get the session from the Server
     */
    void getSession();
    /*
        Delete the session from the Server
     */
    void deleteSession();
    /*
        Request new passwod
     */
    void postForgotPassRequestNew();
    /*
        reset password
     */
    void postForgotPassRequestReset();
    /*

     */
    void postUserPassword();
    void postUserEmail();
    void getUserEmailConfirm();
    void getBalances();
    void getPersonID();

    void getTokens();
    void getTokensTokendID();
    void createToken();
    void toggle_token_like();
    void createClaim();
    void approveClaim();

}
