package com.anushka.androidtutz.contactmanager

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.DialogInterface
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

import com.anushka.androidtutz.contactmanager.adapter.ContactsAdapter
import com.anushka.androidtutz.contactmanager.db.ContactsAppDatabase
import com.anushka.androidtutz.contactmanager.db.entity.Contact

import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var contactsAdapter: ContactsAdapter? = null
    private val contactArrayList = ArrayList<Contact>()
    private var recyclerView: RecyclerView? = null
    private var contactsAppDatabase: ContactsAppDatabase? = null

    internal var callback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            //Toast.makeText(getApplicationContext()," On Create Called ",Toast.LENGTH_LONG).show();
            Log.i(TAG, " on create invoked ")

            createContact("name 1", "email 1")
            createContact("name 2", "email 2")
            createContact("name 3", "email 3")


        }


        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            //  Toast.makeText(getApplicationContext()," On Create Called ",Toast.LENGTH_LONG).show();
            Log.i(TAG, " on open invoked ")

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(" Contacts Manager ")

        recyclerView = findViewById(R.id.recycler_view_contacts)
        contactsAppDatabase = Room.databaseBuilder<ContactsAppDatabase>(applicationContext, ContactsAppDatabase::class.java, "ContactDB").addCallback(callback).build()



        GetAllContactsAsyncTask().execute()

        contactsAdapter = ContactsAdapter(this, contactArrayList, this@MainActivity)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = contactsAdapter


        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { addAndEditContacts(false, null, -1) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }


    fun addAndEditContacts(isUpdate: Boolean, contact: Contact?, position: Int) {
        val layoutInflaterAndroid = LayoutInflater.from(applicationContext)
        val view = layoutInflaterAndroid.inflate(R.layout.layout_add_contact, null)

        val alertDialogBuilderUserInput = AlertDialog.Builder(this@MainActivity)
        alertDialogBuilderUserInput.setView(view)

        val contactTitle = view.findViewById<TextView>(R.id.new_contact_title)
        val newContact = view.findViewById<EditText>(R.id.name)
        val contactEmail = view.findViewById<EditText>(R.id.email)

        contactTitle.text = if (!isUpdate) "Add New Contact" else "Edit Contact"

        if (isUpdate && contact != null) {
            newContact.setText(contact.name)
            contactEmail.setText(contact.email)
        }

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(if (isUpdate) "Update" else "Save") { dialogBox, id -> }
                .setNegativeButton("Delete"
                ) { dialogBox, id ->
                    if (isUpdate) {

                        deleteContact(contact, position)
                    } else {

                        dialogBox.cancel()

                    }
                }


        val alertDialog = alertDialogBuilderUserInput.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(newContact.text.toString())) {
                Toast.makeText(this@MainActivity, "Enter contact name!", Toast.LENGTH_SHORT).show()
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

    private fun deleteContact(contact: Contact?, position: Int) {

        contactArrayList.removeAt(position)

        DeleteContactAsyncTask().execute(contact)

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

    private inner class DeleteContactAsyncTask : AsyncTask<Contact, Void, Void>() {

        override fun doInBackground(vararg contacts: Contact): Void? {

            contactsAppDatabase!!.contactDAO.deleteContact(contacts[0])

            return null
        }

        override fun onPostExecute(aVoid: Void) {
            super.onPostExecute(aVoid)
            contactsAdapter!!.notifyDataSetChanged()
        }
    }

    companion object {
        private val TAG = "MainActivityTag"
    }

}
