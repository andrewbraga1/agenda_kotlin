package com.thiago.unifor.javaapp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


import com.thiago.unifor.javaapp.MainActivity
import com.thiago.unifor.contactmanager.R
import com.thiago.unifor.javaapp.db.entity.Contact

import java.util.ArrayList


class ContactsAdapter(private val context: Context, private val contactssList: ArrayList<Contact>, private val mainActivity: MainActivity) : RecyclerView.Adapter<ContactsAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var name: TextView
        var email: TextView


        init {
            name = view.findViewById(R.id.name)
            email = view.findViewById(R.id.email)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.contact_list_item, parent, false)

        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {


        val contact = contactssList[position]

        holder.name.text = contact.name
        holder.email.text = contact.email

        holder.itemView.setOnClickListener { mainActivity.addOrEditContacts(true, contact, position) }

        // Configurando itens da lista para serem longClickable
        holder.itemView.isLongClickable = true
        holder.itemView.setOnLongClickListener { mainActivity.deleteContact(contact, position);true}

    }

    override fun getItemCount(): Int {

        return contactssList.size
    }


}

