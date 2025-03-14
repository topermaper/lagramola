package com.marcosedo.lagramola;

import android.content.res.Resources;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;


public class HttpUploader {

    private static final int CONNECTION_TIMEOUT = 15000;//miliseconds

    private static final String CHARSET = "utf-8";
    private static final String LINEEND = "\r\n";
    private static final String TWOHYPHENS = "--";
    private static final String BOUNDARY = "*****";

    DataOutputStream dos;
    private HttpURLConnection conn;
    private Resources resources;

    ///////////CONSTRUCTOR//////////
    public HttpUploader(Resources resources, String urlString) throws ConnectionException {
        this.resources = resources;

        try {
            URL connectURL = new URL(urlString);
            conn = (HttpURLConnection) connectURL.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            conn.setInstanceFollowRedirects(false);
            //////////////////////////////////////

            OutputStream os = conn.getOutputStream();
            dos = new DataOutputStream(os);
        }catch (UnknownHostException e) {
            throw new ConnectionException("No internet connection");
        } catch (ConnectException e) {
            throw new ConnectionException("No internet connection");
        } catch (SocketTimeoutException e) {
            throw new ConnectionException("Connection timeout. Server not responding after " + CONNECTION_TIMEOUT + " ms");
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void añadirArgumento(String nombre, String valor) {


        try {

            dos.writeBytes(TWOHYPHENS + BOUNDARY + LINEEND);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + nombre + "\"" + LINEEND);
            dos.writeBytes("Content-Type: text/plain; charset=" + CHARSET + LINEEND + LINEEND);
            dos.write(valor.getBytes(CHARSET));
            //dos.writeBytes(valor);
            dos.writeBytes(LINEEND);

        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void añadirArchivo(String nombre, FileInputStream fileInputStream) {

        try {
            dos.writeBytes(TWOHYPHENS + BOUNDARY + LINEEND);
            dos.writeBytes("Content-Disposition: form-data; name=\"archivo\";filename=\""
                    + nombre + "\"" + LINEEND);
            dos.writeBytes("Content-Type: image/jpeg" + LINEEND + LINEEND);

            //Log.i("NOMBRE ARCHIVO",nombre);
            int bytesAvailable = fileInputStream.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            // Close input stream
            fileInputStream.close();
            dos.writeBytes(LINEEND);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addImageAsByteArray(String nombre, byte[] byteArray) {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray,0,byteArray.length);


        try{
            dos.writeBytes(TWOHYPHENS + BOUNDARY + LINEEND);
            dos.writeBytes("Content-Disposition: form-data; name=\"archivo\";filename=\""
                    + nombre + "\"" + LINEEND);
            dos.writeBytes("Content-Type: image/jpeg" + LINEEND + LINEEND);

            int bytesAvailable = byteArrayInputStream.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = byteArrayInputStream.read(buffer,0,bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = byteArrayInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);
            }

            byteArrayInputStream.close();// Close input stream
            dos.writeBytes(LINEEND);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject enviar() {

        JSONObject json = null;

        try {
            //esto indica que es el final de lo que se envia
            dos.writeBytes(TWOHYPHENS + BOUNDARY + TWOHYPHENS + LINEEND);
            dos.flush();

            ////////////////////////////////////////////////////////////
            //LEEMOS LA SALIDA DEL SCRIPT PHP Y
            // CREAMOS EL OBJETO JSON A PARTIR DE LA RESPUESTA DEL SERVIDOR
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                Log.w("PHP OUTPUT", line);
                stringBuilder.append(line + "\n");
            }
            dos.close();
            json = new JSONObject(stringBuilder.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return json;

    }


}
