package mx.tecnm.tepic.U1_Practica2_GuardarMemoria

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import android.widget.ArrayAdapter
import mx.tecnm.tepic.U1_Practica2_GuardarMemoria.databinding.ActivityMainBinding
import android.content.Intent
import android.widget.RadioButton


private lateinit var b: ActivityMainBinding
class MainActivity : AppCompatActivity() {
        var nombreArchivo = ""
        var texto = ""
        var extdata = listOf<String>()
        var cosas = ArrayList<String>()
        var files = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)


        var cosas = ArrayList<String>()
        var spinner = this.spinner
        var grupo = b.grupo


        val rbE: RadioButton = rbtnExterna as RadioButton
        rbE.setOnClickListener(first_radio_listener)

        val rbI: RadioButton = rbtnInterna as RadioButton
        rbI.setOnClickListener(second_radio_listener)

        btnGuardar.setOnClickListener {
            nombreArchivo = txtTitulo.text.toString()
            texto = txtContenido.text.toString()
            if (rbtnInterna.isChecked){
                leerDatosInterna()
                if (guardarInterna()){
                    Toast.makeText(this, "Archivo interno guardado correctamente", Toast.LENGTH_LONG).show()
                    txtContenido.setText("")
                    txtTitulo.setText("")
                }else{
                    Toast.makeText(this, "Error al guardar los datos internos", Toast.LENGTH_LONG).show()
                }
            }
            if (rbtnExterna.isChecked){
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0 )
                }
                leerDatosSD()
                if (guardarExterna()){
                    Toast.makeText(this, "Datos externos guardados correctamente", Toast.LENGTH_LONG).show()
                    txtContenido.setText("")
                    txtTitulo.setText("")
                }else{
                    Toast.makeText(this, "Error al guardar los datos externos", Toast.LENGTH_LONG).show()
                }

            }
        }

        btnAbrir.setOnClickListener {
            nombreArchivo = txtTitulo.text.toString()
            var data = abrirInterna()
            if (rbtnInterna.isChecked){
                if (!data.isNullOrEmpty()){
                    for(i in data){
                        txtContenido.setText(txtContenido.text.toString()+"\n"+i)
                    }
                    Toast.makeText(this, "Datos Cargados Correctamente", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "Error al cargar los datos internos", Toast.LENGTH_LONG).show()
                    txtContenido.setText("")
                }
            }
            if (rbtnExterna.isChecked){
                if (abrirExterna()){
                    Toast.makeText(this, "Datos cargados correctamente", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "Error al cargar los datos externos", Toast.LENGTH_LONG).show()
                    txtContenido.setText("")
                }
            }
        }
    }

    var first_radio_listener: View.OnClickListener = object : View.OnClickListener {

        override fun  onClick(v: View?) {
            cosas.clear()
            leerDatosSD()
        }
    }

    var second_radio_listener: View.OnClickListener = object : View.OnClickListener {

        override fun  onClick(v: View?) {
            cosas.clear()
            leerDatosInterna()
        }
    }
    //nunca se usa
    fun SplashScreen(){
            var activity2 = Intent(this,MainActivity2::class.java)
            startActivity(activity2)

    }

    fun leerDatosSD(){
        val externalStorageVolumes: Array<out File> =
            ContextCompat.getExternalFilesDirs(applicationContext, null)
        val primaryExternalStorage = externalStorageVolumes[0]
        val filesE: Array<File> = File(primaryExternalStorage.absoluteFile.toURI()).listFiles()
        cosas.clear()
        cosas.add("Notas Externas")
        filesE.forEach {
            val externo = File(primaryExternalStorage,it.name)
            cosas.add(it.name);

        }
        val arrayAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cosas)
        spinner.adapter = arrayAdapter
    }

    fun leerDatosInterna(){
        cosas.clear()
        if(this.fileList().size == 1){
            files.clear()
            cosas.add("Notas internas")
            files.add(this.fileList()[0])
        }
        else if(this.fileList().size >= 1){
            files.clear()
            cosas.add("Notas internas")
            files = this.fileList().toList() as ArrayList<String>
        }

        files.forEach {
            openFileInput(it).bufferedReader().useLines { lines ->
                val texto =lines.fold("") { some, text ->
                    "$some\n$text"
                }
                cosas.add(it)
            }
        }

        val arrayAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cosas)
        spinner.adapter = arrayAdapter


    }

    private fun guardarInterna(): Boolean{
        try {
            var fs = OutputStreamWriter(openFileOutput(nombreArchivo, MODE_PRIVATE))
            fs.write(texto)
            fs.flush()
            fs.close()

        }catch (io: IOException){
            return false
        }
        leerDatosInterna()
        return true
    }

    private fun abrirInterna():List<String>{
        var line= listOf<String>()
        if (fileList().contains(nombreArchivo)){
            try {
                val fe = InputStreamReader(openFileInput(nombreArchivo))
                val br = BufferedReader(fe)
                line = br.readLines()
                br.close()
                fe.close()
                Toast.makeText(this, "Se ha cargado el archivo con exito", Toast.LENGTH_SHORT).show()
            }catch (io : IOException){
                Toast.makeText(this, "Error con los datos internos", Toast.LENGTH_SHORT).show()
                return emptyList()
            }
        }
        return line
    }

    private fun guardarExterna():Boolean{
        texto = txtContenido.text.toString()
        nombreArchivo = txtTitulo.text.toString()
        try{
            val externalStorageVolumes: Array<out File> =
                ContextCompat.getExternalFilesDirs(applicationContext, null)
            val primaryExternalStorage = externalStorageVolumes[0]
            val file = File(primaryExternalStorage, texto.toString())
            val contenido = texto+","+nombreArchivo
            file.writeText(contenido)
            file.createNewFile()
            Toast.makeText(this, "Se ha guardado el archivo con exito", Toast.LENGTH_SHORT).show()
        }
        catch (io: SecurityException) {
            Toast.makeText(this,"Ocurrio un error al guardar",Toast.LENGTH_SHORT).show()
            return false
        }
        leerDatosSD()
        return true
    }

    private fun abrirExterna():Boolean{
        try
        {

            if(Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED)
            {
                Toast.makeText(this,"Error al leer memoria externa",Toast.LENGTH_SHORT).show()
                return false
            }
            var pathSD=Environment.getExternalStorageDirectory()
            var fileSD=File(pathSD.absolutePath,nombreArchivo)
            if(fileSD.exists())
            {
                val fi = InputStreamReader(FileInputStream(fileSD))
                val br = BufferedReader(fi)
                extdata = br.readLines()
                br.close()
                fi.close()
                this.txtContenido.setText(extdata.toString())
                Toast.makeText(this,"Exito!",Toast.LENGTH_SHORT).show()
            }
            else
            {
            }
        }
        catch (IO: java.lang.Exception)
        {
            Toast.makeText(this,"Ocurrio un error al abrir el archivo",Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}