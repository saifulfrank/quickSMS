package quick.sms.quicksms.ui

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.notification_template_custom_big.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import quick.sms.quicksms.BaseActivity
import quick.sms.quicksms.R
import quick.sms.quicksms.backend.Contact

class ContactsActivity : BaseActivity() {

    private var tileNumber = 0
    private var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contacts = (intent.extras.get("contacts") as List<Contact>).sortedBy { it.name }
        tileNumber = intent.getIntExtra("tile_number", 0)
        ContactsLayout(contentResolver, contacts) { selectContact(it) }.setContentView(this)

        doAsync {
            MobileAds.initialize(applicationContext, "ca-app-pub-2206499302575732~5712613107\n")
            mAdView = findViewById<View>(R.id.adView) as AdView
            val adRequest = AdRequest.Builder().build()
            uiThread {
                mAdView!!.loadAd(adRequest)
            }
        }
    }

    private fun selectContact(contact: Contact) {
        val returnIntent = Intent()
        returnIntent.putExtra("tile_number", tileNumber)
        returnIntent.putExtra("chosen_contact", contact)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private class ContactsLayout(val cr: ContentResolver, val contacts: List<Contact>,
                                 val selectContact: (Contact) -> Unit)
        : AnkoComponent<ContactsActivity> {

        override fun createView(ui: AnkoContext<ContactsActivity>) = with(ui) {
            scrollView {
                verticalLayout {
                    for (contact in contacts) {
                        contactView(contact)
                    }
                }
            }
        }

        fun _LinearLayout.contactView(contact: Contact) {
            linearLayout {
                var photo = contact.image?.let {
                    try {
                        val inStream = cr.openInputStream(Uri.parse(it))
                        Drawable.createFromStream(inStream, it)
                    } catch (e: Exception) {

                    }
                }//?: resources.getDrawable(R.drawable.default_image, context.theme)
                if (photo == null) {
                    photo = if (android.os.Build.VERSION.SDK_INT >= 21) {
                        resources.getDrawable(R.drawable.default_image, context.theme)
                    } else {
                        resources.getDrawable(R.drawable.default_image)
                    }
                }
                imageView {
                    if(photo is Drawable){
                        //Check that photo is of type drawable
                        image = photo
                    }
                }.lparams {
                    gravity = Gravity.START
                }
                textView(contact.name) {
                    textSize = sp(10).toFloat()
                }.lparams(width = matchParent) {
                    leftMargin = dip(10)
                    gravity = Gravity.END
                }
                onClick {
                    selectContact(contact)
                }
            }.lparams(width = matchParent)
        }
    }
}
