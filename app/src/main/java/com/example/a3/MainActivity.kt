import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import com.example.a3.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String
)

@Dao
interface ContactDao {
    @Insert
    fun insert(contact: Contact)

    @Update
    fun update(contact: Contact)

    @Delete
    fun delete(contact: Contact)

    @Query("SELECT * FROM contact")
    fun getAllContacts(): List<Contact>
}

@Database(entities = [Contact::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}

class MainActivity : AppCompatActivity() {

    private lateinit var contactDao: ContactDao
    private lateinit var adapter: ContactAdapter

    companion object {
        const val CONTACT_PICK_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ContactAdapter()
        recyclerView.adapter = adapter

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "contact-database"
        ).build()

        contactDao = db.contactDao()

        fetchData()

        // Button to request contacts
        val importButton: View = findViewById(R.id.importButton)
        importButton.setOnClickListener {
            requestContacts()
        }
    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            val contacts = contactDao.getAllContacts()
            runOnUiThread {
                adapter.setContacts(contacts)
            }
        }
    }

    private fun requestContacts() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_PICK_REQUEST && resultCode == Activity.RESULT_OK) {
            val contactUri = data?.data
            if (contactUri != null) {
                // Handle selected contacts data
                // For simplicity, assuming you have a method to extract contact details
                val contact = extractContactDetails(contactUri)

                // Save the contact to the database
                saveContact(contact)

                // Update the UI
                fetchData()
            }
        }
    }

    private fun extractContactDetails(contactUri: String): Contact {
        // Implement logic to extract contact details from the URI
        // For simplicity, let's assume we have a method for this
        return Contact(name = "John Doe", phoneNumber = "123-456-7890", email = "john@example.com")
    }

    private fun saveContact(contact: Contact) {
        GlobalScope.launch(Dispatchers.IO) {
            contactDao.insert(contact)
        }
    }

    // Implement other methods like update, delete, call, message as needed
}



class ContactAdapter : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contacts: List<Contact> = listOf()

    fun setContacts(contacts: List<Contact>) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contact: Contact) {
            // Implement binding logic for each contact item
        }
    }
}
