package com.example.assignment2
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    private var availableCars = CarData.cars.toMutableList()
    private val favouriteCars = mutableListOf<Car>()
    private val bookings = mutableListOf<String>()
    private var balance = 500
    private var currentIndex = 0
    private var darkMode = false
    private lateinit var carNameText: TextView
    private lateinit var carImage: ImageView
    private lateinit var carDetailsText: TextView
    private lateinit var carRating: RatingBar
    private lateinit var balanceText: TextView
    private lateinit var searchInput: EditText
    private lateinit var sortSpinner: Spinner
    private lateinit var darkModeSwitch: Switch
    private lateinit var favoriteButton: Button
    private lateinit var favouritesLayout: LinearLayout
    private lateinit var bookingsLayout: LinearLayout
    private lateinit var rootLayout: ScrollView
    private var displayedCars = listOf<Car>()
    private val rentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {

                val data = result.data

                @Suppress("DEPRECATION")
                val rentedCarId = data?.getIntExtra("carId", -1) ?: -1
                val rentedCar = availableCars.find { it.id == rentedCarId }

                val days = data?.getIntExtra("days", 1) ?: 1
                val cost = data?.getIntExtra("cost", 0) ?: 0

                if (rentedCar != null &&
                    availableCars.any { it.id == rentedCar.id } &&
                    cost <= 400 &&
                    cost <= balance
                ) {
                    availableCars.removeAll { it.id == rentedCar.id }
                    favouriteCars.removeAll { it.id == rentedCar.id }

                    balance -= cost

                    bookings.add(
                        "${rentedCar.name} ${rentedCar.model} - " +
                                "$days days - $cost credits"
                    )

                    Toast.makeText(
                        this,
                        "Booking successful!",
                        Toast.LENGTH_SHORT
                    ).show()

                    currentIndex = 0
                    updateScreen()
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        carNameText = findViewById(R.id.carNameText)
        carImage = findViewById(R.id.carImage)
        carDetailsText = findViewById(R.id.carDetailsText)
        carRating = findViewById(R.id.carRating)
        balanceText = findViewById(R.id.balanceText)
        searchInput = findViewById(R.id.searchInput)
        sortSpinner = findViewById(R.id.sortSpinner)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        favoriteButton = findViewById(R.id.favoriteButton)
        favouritesLayout = findViewById(R.id.favouritesLayout)
        bookingsLayout = findViewById(R.id.bookingsLayout)
        rootLayout = findViewById(R.id.mainRoot)
        setupSort()
        setupSearch()
        setupButtons()
        updateScreen()
    }
    private fun setupSort() {
        val options = arrayOf(
            "Default",
            "Rating: High to Low",
            "Year: Newest First",
            "Cost: Low to High"
        )
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        sortSpinner.adapter = adapter

        sortSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    currentIndex = 0
                    updateScreen()
                }
            }
    }
    private fun setupSearch() {

        searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                currentIndex = 0
                updateScreen()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun setupButtons() {

        findViewById<Button>(R.id.nextButton).setOnClickListener {

            if (displayedCars.isNotEmpty()) {
                currentIndex++

                if (currentIndex >= displayedCars.size) {
                    currentIndex = 0
                }

                updateScreen()
            }
        }
        favoriteButton.setOnClickListener {
            val car = getCurrentCar()
            if (car != null) {
                if (favouriteCars.any { it.id == car.id }) {
                    favouriteCars.removeAll { it.id == car.id }
                    Toast.makeText(this, "Removed from favourites", Toast.LENGTH_SHORT).show()
                } else {
                    favouriteCars.add(car)
                    Toast.makeText(this, "Added to favourites", Toast.LENGTH_SHORT).show()
                }
                updateScreen()
            }
        }
        findViewById<Button>(R.id.rentButton).setOnClickListener {
            val car = getCurrentCar()
            if (car != null) {
                val intent = Intent(this, RentActivity::class.java)
                intent.putExtra("carId", car.id)
                intent.putExtra("carName", car.name)
                intent.putExtra("carModel", car.model)
                intent.putExtra("carYear", car.year)
                intent.putExtra("carRating", car.rating)
                intent.putExtra("carKm", car.kilometres)
                intent.putExtra("carCost", car.dailyCost)
                intent.putExtra("carImage", car.image)
                intent.putExtra("balance", balance)
                intent.putExtra("darkMode", darkMode)
                rentLauncher.launch(intent)
            }
        }
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            darkMode = isChecked
            if (darkMode) {
                rootLayout.setBackgroundColor(android.graphics.Color.DKGRAY)
                carNameText.setTextColor(android.graphics.Color.WHITE)
                carDetailsText.setTextColor(android.graphics.Color.WHITE)
                balanceText.setTextColor(android.graphics.Color.WHITE)
            } else {
                rootLayout.setBackgroundColor(android.graphics.Color.WHITE)
                carNameText.setTextColor(android.graphics.Color.BLACK)
                carDetailsText.setTextColor(android.graphics.Color.BLACK)
                balanceText.setTextColor(android.graphics.Color.BLACK)
            }
        }
    }
    private fun getCurrentCar(): Car? {
        return if (displayedCars.isNotEmpty() &&
            currentIndex in displayedCars.indices
        ) {
            displayedCars[currentIndex]
        } else {
            null
        }
    }
    private fun updateScreen() {
        val keyword = searchInput.text.toString().trim()
        displayedCars = availableCars.filter {
            it.name.contains(keyword, ignoreCase = true) ||
                    it.model.contains(keyword, ignoreCase = true)
        }
        displayedCars = when (sortSpinner.selectedItemPosition) {
            1 -> displayedCars.sortedByDescending { it.rating }
            2 -> displayedCars.sortedByDescending { it.year }
            3 -> displayedCars.sortedBy { it.dailyCost }
            else -> displayedCars
        }
        balanceText.text = "Your balance: $balance credits"
        val car = getCurrentCar()
        if (car == null) {
            carNameText.text = "No cars found"
            carImage.setImageDrawable(null)
            carDetailsText.text = "No available cars match your search."
            carRating.rating = 0f
            favoriteButton.isEnabled = false
        } else {
            carNameText.text = "${car.name} ${car.model}"
            carImage.setImageResource(car.image)
            carDetailsText.text =
                "Year: ${car.year}\n" +
                        "Kilometres: ${car.kilometres} km\n" +
                        "Daily cost: ${car.dailyCost} credits"
            carRating.rating = car.rating
            favoriteButton.isEnabled = true
            if (favouriteCars.any { it.id == car.id }) {
                favoriteButton.text = "Remove from favourites"
            } else {
                favoriteButton.text = "Add to favourites"
            }
        }
        updateFavourites()
        updateBookings()
    }
    private fun updateFavourites() {
        favouritesLayout.removeAllViews()
        if (favouriteCars.isEmpty()) {
            addText(favouritesLayout, "No favourite cars yet.")
        } else {
            for (car in favouriteCars) {
                addText(
                    favouritesLayout,
                    "${car.name} ${car.model} (${car.year})"
                )
            }
        }
    }
    private fun updateBookings() {
        bookingsLayout.removeAllViews()
        if (bookings.isEmpty()) {
            addText(bookingsLayout, "No bookings yet.")
        } else {
            for (booking in bookings) {
                addText(bookingsLayout, booking)
            }
        }
    }
    private fun addText(layout: LinearLayout, message: String) {
        val textView = TextView(this)
        textView.text = message
        textView.textSize = 16f
        textView.setPadding(8, 12, 8, 12)
        layout.addView(textView)
    }
}
