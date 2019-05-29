package com.thiago.unifor.javaapp

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.thiago.unifor.contactmanager.R

import com.thiago.unifor.javaapp.adapter.ContactsAdapter
import com.thiago.unifor.javaapp.db.ContactsAppDatabase
import com.thiago.unifor.javaapp.db.entity.Contact

import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var contactsAdapter: ContactsAdapter? = null
    private val contactArrayList = ArrayList<Contact>()
    private var recyclerView: RecyclerView? = null
    private var contactsAppDatabase: ContactsAppDatabase? = null

    internal var callback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recycler_view_contacts)
        contactsAppDatabase = Room.databaseBuilder<ContactsAppDatabase>(applicationContext, ContactsAppDatabase::class.java, "ContactDB").addCallback(callback).build()


        // Carregar todos os contatos salvos
        GetAllContactsAsyncTask().execute()

        contactsAdapter = ContactsAdapter(this, contactArrayList, this@MainActivity)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = contactsAdapter

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { addOrEditContacts(false, null, -1) }
    }


    // Métodos responável pelo adicionar ou pelo atualizar a depender da origem do comando
    fun addOrEditContacts(isUpdate: Boolean, contact: Contact?, position: Int) {
        val layoutInflaterAndroid = LayoutInflater.from(applicationContext)
        val view = layoutInflaterAndroid.inflate(R.layout.layout_add_contact, null)


        //Criação de modal
        val alertDialogBuilderUserInput = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilderUserInput.setView(view)

        val contactTitle = view.findViewById<TextView>(R.id.new_contact_title)
        val newContact = view.findViewById<EditText>(R.id.name)
        val contactEmail = view.findViewById<EditText>(R.id.email)

        contactTitle.text = if (!isUpdate) contactTitle.text else "Edit Contact"

        if (isUpdate && contact != null) {
            newContact.setText(contact.name)
            contactEmail.setText(contact.email)
        }

        alertDialogBuilderUserInput
                .setCancelable(true)
                .setPositiveButton("OK") { dialogBox, id -> }
                .setNegativeButton("Cancel"
                ) { dialogBox, id ->

                    dialogBox.cancel()
                }


        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(newContact.text.toString())) {
                Toast.makeText(this@MainActivity, "You must enter a contact!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } else {
                alertDialog.dismiss()
            }


            if (isUpdate && contact != null) {

                updateContact(newContact.text.toString(), contactEmail.text.toString(), position)
            } else {

                createContact(newContact.text.toString(), contactEmail.text.toString())
            }
        })
    }

    fun deleteContact(contact: Contact?, position: Int) {
        val layoutInflaterAndroid = LayoutInflater.from(applicationContext)
        val view = layoutInflaterAndroid.inflate(R.layout.layout_confirm_delete_contact, null)


        //Criação de modal
        val alertDialogBuilderUserInput = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilderUserInput.setView(view)

        alertDialogBuilderUserInput
                .setCancelable(true)
                .setPositiveButton("OK") { dialogBox, id ->
                    contactArrayList.removeAt(position)

                    DeleteContactAsyncTask().execute(contact)
                }
                .setNegativeButton("Cancel"
                ) { dialogBox, id ->

                    dialogBox.cancel()
                }


        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()



    }

    private fun updateContact(name: String, email: String, position: Int) {

        val contact = contactArrayList[position]

        contact.name = name
        contact.email = email



        UpdateContactAsyncTask().execute(contact)

        contactArrayList[position] = contact


    }

    private fun createContact(name: String, email: String) {

        CreateContactAsyncTask().execute(Contact(0, name, email))

    }

    private inner class GetAllContactsAsyncTask : AsyncTask<Void, Void, Void>() {


        override fun doInBackground(vararg voids: Void?): Void? {

            contactArrayList.addAll(contactsAppDatabase!!.contactDAO.contacts)
            return null
        }


        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            contactsAdapter!!.notifyDataSetChanged()
        }
    }


    private inner class CreateContactAsyncTask : AsyncTask<Contact, Void, Void>() {


        override fun doInBackground(vararg contacts: Contact): Void? {

            val id = contactsAppDatabase!!.contactDAO.addContact(contacts[0])


            val contact = contactsAppDatabase!!.contactDAO.getContact(id)

            if (contact != null) {

                contactArrayList.add(0, contact)


            }

            return null
        }


        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            contactsAdapter!!.notifyDataSetChanged()
        }
    }

    private inner class UpdateContactAsyncTask : AsyncTask<Contact, Void, Void>() {


        override fun doInBackground(vararg contacts: Contact): Void? {

            contactsAppDatabase!!.contactDAO.updateContact(contacts[0])
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            contactsAdapter!!.notifyDataSetChanged()
        }
    }

    private inner class DeleteContactAsyncTask : AsyncTask<Contact, Void?, Void?>() {

        override fun doInBackground(vararg contacts: Contact): Void? {

            contactsAppDatabase!!.contactDAO.deleteContact(contacts[0])

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            contactsAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {
        private val TAG = "MainActivityTag"
    }

}


