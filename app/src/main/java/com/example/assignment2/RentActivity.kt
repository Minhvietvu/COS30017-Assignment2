package com.example.assignment2
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RentActivity : AppCompatActivity() {

    private var selectedCar: Car? = null
    private var balance = 500
    private var rentalDays = 1
    private var totalCost = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent)
        val isDarkMode = intent.getBooleanExtra("darkMode", false)

        val rentRoot = findViewById<android.view.View>(R.id.rentRoot)

        if (isDarkMode) {
            rentRoot.setBackgroundColor(android.graphics.Color.DKGRAY)
        } else {
            rentRoot.setBackgroundColor(android.graphics.Color.WHITE)
        }
        val textViews = listOf(
            R.id.rentCarName,
            R.id.rentCarDetails,
            R.id.daysLabel,
            R.id.totalCostText,
            R.id.rentBalanceText,
            R.id.errorText
        )

        for (id in textViews) {
            val text = findViewById<TextView>(id)

            if (isDarkMode) {
                text.setTextColor(android.graphics.Color.WHITE)
            } else {
                text.setTextColor(android.graphics.Color.BLACK)
            }
        }

        selectedCar = Car(
            intent.getIntExtra("carId", 0),
            intent.getStringExtra("carName") ?: "",
            intent.getStringExtra("carModel") ?: "",
            intent.getIntExtra("carYear", 0),
            intent.getFloatExtra("carRating", 0f),
            intent.getIntExtra("carKm", 0),
            intent.getIntExtra("carCost", 0),
            intent.getIntExtra("carImage", 0)
        )
        balance = intent.getIntExtra("balance", 500)

        val car = selectedCar

        if (car == null) {
            Toast.makeText(this, "Car information is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val carName = findViewById<TextView>(R.id.rentCarName)
        val carDetails = findViewById<TextView>(R.id.rentCarDetails)
        val daysLabel = findViewById<TextView>(R.id.daysLabel)
        val totalText = findViewById<TextView>(R.id.totalCostText)
        val balanceText = findViewById<TextView>(R.id.rentBalanceText)
        val errorText = findViewById<TextView>(R.id.errorText)
        val daysSeekBar = findViewById<SeekBar>(R.id.daysSeekBar)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        carName.text = "${car.name} ${car.model}"
        carDetails.text = "Year: ${car.year}\nDaily cost: ${car.dailyCost} credits"
        balanceText.text = "Your balance: $balance credits"

        fun updateCost() {
            totalCost = car.dailyCost * rentalDays
            daysLabel.text = "Rental days: $rentalDays"
            totalText.text = "Total cost: $totalCost credits"

            errorText.text = when {
                totalCost > 400 -> "Maximum rental cost is 400 credits."
                totalCost > balance -> "Not enough credits."
                else -> ""
            }
        }

        daysSeekBar.max = 6
        daysSeekBar.progress = 0

        daysSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                rentalDays = progress + 1
                updateCost()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        updateCost()

        saveButton.setOnClickListener {
            if (totalCost > 400 || totalCost > balance) {
                Toast.makeText(
                    this,
                    "Cannot complete this booking.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val result = intent
                result.putExtra("carId", car.id)
                result.putExtra("days", rentalDays)
                result.putExtra("cost", totalCost)

                setResult(Activity.RESULT_OK, result)
                Toast.makeText(this, "Booking saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            Toast.makeText(this, "Booking cancelled.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
