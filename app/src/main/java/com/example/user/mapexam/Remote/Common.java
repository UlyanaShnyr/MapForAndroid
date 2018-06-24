package com.example.user.mapexam.Remote;

public class Common {

    private static final String GOOGLE_API_URL="https://api.privatbank.ua";
    public static  IGoogleAPIService getGoogleApiService(){
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);

    }
}
