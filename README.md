# CycloneConnect

### AI-Powered Cyclone Intelligence and Disaster Management System

CycloneConnect is a research-oriented cyclone intelligence and
disaster-management project focused on the **North Indian Ocean
region**, particularly the Bay of Bengal and Arabian Sea.

The project combines Big Data Analytics, Deep Learning, and Android
application development to address two important challenges during
tropical cyclones:

-   **Cyclone Intelligence:** Analysing satellite imagery to estimate
    cyclone wind speed and intensity.
-   **Disaster Response:** Providing a mobile platform for accessing
    cyclone-related information and supporting emergency coordination,
    including proposed offline communication capabilities.

The system aims to bridge the gap between cyclone analysis and practical
disaster response through an integrated architecture.

------------------------------------------------------------------------

## 1. Problem Statement

Tropical cyclones in the North Indian Ocean pose significant threats to
coastal communities, resulting in loss of life, infrastructure damage,
and communication disruptions.

Conventional cyclone intensity estimation relies heavily on manual
interpretation of satellite imagery. Although deep learning offers
opportunities for automated analysis, training these models requires
large volumes of labelled spatio-temporal data.

Furthermore, during disasters, internet and cellular networks may become
unavailable, making emergency communication and resource coordination
difficult.

**CycloneConnect addresses these challenges through a dual-component
approach:**

1.  A distributed Big Data and Deep Learning pipeline for
    satellite-based cyclone wind-speed estimation.
2.  An Android disaster-management application designed to provide
    cyclone intelligence and support emergency response, with offline
    communication planned through Bluetooth Low Energy (BLE).

------------------------------------------------------------------------

## 2. System Architecture

CycloneConnect follows a modular architecture consisting of a
distributed data-processing pipeline, a deep learning subsystem, a
backend API, and an Android application.

### High-Level Architecture

``` mermaid
flowchart TD
    A[NOAA GridSat-B1<br/>Satellite Imagery]
    B[IBTrACS<br/>Cyclone Track Records]

    A --> C[Data Acquisition]
    B --> C

    C --> D[Hadoop HDFS<br/>Distributed Storage]
    D --> E[Apache Spark<br/>Distributed Processing]

    E --> F[Data Cleaning and Alignment]
    F --> G[Storm-Centred Image Patches]
    G --> H[Processed Dataset]
    G --> R[MongoDB]

    H --> I[Deep Learning]
    I --> J[CNN]
    I --> K[ConvLSTM]

    J --> L[Wind Speed Prediction]
    K --> L

    L --> M[FastAPI Backend]
    M --> N[Android Application]

    N --> O[Cyclone Intelligence]
    N --> P[Emergency Response]

    P -. Planned .-> Q[BLE Offline Communication]
```

### Architecture Components

<table>
  <thead>
    <tr>
      <th>Component</th>
      <th>Technology</th>
      <th>Responsibility</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Data Acquisition</td>
      <td>NOAA, IBTrACS</td>
      <td>Obtain satellite imagery and cyclone records</td>
    </tr>
    <tr>
      <td>Distributed Storage</td>
      <td>Hadoop HDFS</td>
      <td>Store large-scale satellite and processed datasets</td>
    </tr>
    <tr>
      <td>Distributed Processing</td>
      <td>Apache Spark, PySpark</td>
      <td>Clean, align, crop and prepare satellite data</td>
    </tr>
    <tr>
      <td>Data Analysis</td>
      <td>MongoDB</td>
      <td>Support storm-record storage and complex queries</td>
    </tr>
    <tr>
      <td>Deep Learning</td>
      <td>TensorFlow/Keras</td>
      <td>Train CNN and ConvLSTM models</td>
    </tr>
    <tr>
      <td>Backend</td>
      <td>FastAPI</td>
      <td>Expose cyclone-related data through REST endpoints</td>
    </tr>
    <tr>
      <td>Mobile Application</td>
      <td>Kotlin, Jetpack Compose</td>
      <td>Provide the user-facing disaster-management interface</td>
    </tr>
    <tr>
      <td>On-Device Inference</td>
      <td>TensorFlow Lite</td>
      <td>Execute the bundled CNN model on Android</td>
    </tr>
  </tbody>
</table>

### Data Flow

1.  **Acquisition:** Download NOAA GridSat-B1 infrared satellite imagery
    and IBTrACS cyclone best-track records.
2.  **Ingestion:** Load the datasets into HDFS for distributed storage.
3.  **Processing:** Use Spark to clean cyclone records, align
    timestamps, match satellite scans, and extract storm-centred image
    patches.
4.  **Storage:** Store the processed datasets in Parquet format, with
    image patches represented as numerical arrays.
5.  **Model Training:** Prepare the processed images for CNN and
    ConvLSTM training.
6.  **Prediction:** Generate wind-speed estimates using trained deep
    learning models.
7.  **Integration:** Expose cyclone information through the backend and
    integrate prediction functionality into the Android application.

------------------------------------------------------------------------

## 3. Big Data Analytics

The Big Data component addresses the computational challenges involved
in processing large-scale satellite imagery.

### Datasets

**NOAA GridSat-B1**

-   Source:
    https://www.ncei.noaa.gov/data/geostationary-ir-channel-brightness-temperature-gridsat-b1/access/
-   Format: NetCDF
-   Contains global infrared brightness-temperature imagery.
-   Provides satellite observations at three-hour intervals.

**IBTrACS (International Best Track Archive for Climate Stewardship)**

-   Source:
    https://data.humdata.org/dataset/96b309bf-cedb-4f63-8ca3-eb56cdcae876/
-   Format: CSV
-   Contains cyclone identifiers, timestamps, geographical coordinates,
    and wind-speed observations.

### Distributed Processing Pipeline

The project uses Hadoop HDFS and Apache Spark in a two-laptop
distributed environment.

**Hadoop HDFS** - Provides distributed storage across multiple
machines. - Supports block replication and fault tolerance. - Allows
both machines to access a shared dataset.

**Apache Spark** - Performs distributed data processing using PySpark. -
Processes satellite files in parallel. - Handles data cleaning,
timestamp matching, image cropping, and dataset preparation.

**Image Preprocessing** - Matches IBTrACS cyclone records with
corresponding satellite observations. - Extracts fixed-size 64 × 64
pixel patches around cyclone centres. - Retains infrared image channels
for downstream experiments. - Stores processed patches in Parquet
format.

<h3>Intensity Classification Reference</h3>

<p>
  Wind speed observations are classified according to the India Meteorological Department (IMD) scale:
</p>

<table>
  <thead>
    <tr>
      <th>Category</th>
      <th>Abbreviation</th>
      <th>Wind Speed (knots)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Depression</td>
      <td>D</td>
      <td>17–27</td>
    </tr>
    <tr>
      <td>Deep Depression</td>
      <td>DD</td>
      <td>28–33</td>
    </tr>
    <tr>
      <td>Cyclonic Storm</td>
      <td>CS</td>
      <td>34–47</td>
    </tr>
    <tr>
      <td>Severe Cyclonic Storm</td>
      <td>SCS</td>
      <td>48–63</td>
    </tr>
    <tr>
      <td>Very Severe Cyclonic Storm</td>
      <td>VSCS</td>
      <td>64–89</td>
    </tr>
    <tr>
      <td>Extremely Severe Cyclonic Storm</td>
      <td>ESCS</td>
      <td>90–119</td>
    </tr>
    <tr>
      <td>Super Cyclonic Storm</td>
      <td>SuCS</td>
      <td>≥120</td>
    </tr>
  </tbody>
</table>

<p>
  <strong>Note:</strong> KT (knot) = nautical miles per hour.
  1 KT = 1.85 km/h.
</p>

## 4. Deep Learning

The Deep Learning component explores automated cyclone wind-speed
estimation from satellite imagery.

The processed satellite patches are used to train and evaluate two
neural network architectures.

### CNN -- Baseline Model

A Convolutional Neural Network processes a single satellite image.

-   Input: 64 × 64 satellite image.
-   Extracts spatial features from cyclone cloud patterns.
-   Uses convolutional layers, batch normalization, and pooling.
-   Predicts cyclone wind speed in knots.

### ConvLSTM -- Temporal Model

A Convolutional Long Short-Term Memory network processes consecutive
satellite images to learn spatial and temporal patterns.

-   Input: Four satellite images.
-   Each image is separated by three hours.
-   Learns how cyclone structures evolve over time.
-   Predicts cyclone wind speed in knots.

### Model Comparison

The CNN and ConvLSTM experiments were evaluated on the same 1,013 test
observations.

<table>
  <thead>
    <tr>
      <th>Evaluation Metric</th>
      <th>CNN</th>
      <th>ConvLSTM</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>MAE</td>
      <td>12.8231 knots</td>
      <td>12.5253 knots</td>
    </tr>
    <tr>
      <td>RMSE</td>
      <td>17.6671 knots</td>
      <td>17.0156 knots</td>
    </tr>
  </tbody>
</table>
**Observation:** ConvLSTM achieved slightly better performance, reducing
MAE by 2.32% and RMSE by 3.69% compared with the CNN baseline.

These results apply to the evaluated experimental dataset and should not
be interpreted as operational forecasting accuracy.

### Model Evaluation

The project uses:

-   Mean Absolute Error (MAE)
-   Root Mean Squared Error (RMSE)
-   Training and validation loss
-   Comparative evaluation of CNN and ConvLSTM

------------------------------------------------------------------------

## 5. Android Application

The Android application serves as the user-facing component of
CycloneConnect.

It is developed using Kotlin and Jetpack Compose, with a focus on
accessibility and disaster-response functionality.

### Current Application Structure

-   Home dashboard
-   Location search interface
-   Emergency information section
-   Family and friends section
-   Official cyclone alerts section
-   Regional news section
-   Emergency SOS entry point
-   Volunteer and donation sections
-   Settings and theme customization
-   Experimental cyclone prediction demonstration

### On-Device Cyclone Prediction

The current Android prediction demonstration uses TensorFlow Lite.

The application:

1.  Loads a bundled CNN model (`cyclone_cnn.tflite`).
2.  Reads preprocessing parameters from `preprocessing.json`.
3.  Loads a sample 64 × 64 Channel 1 satellite patch.
4.  Normalizes the image using the stored training mean and standard
    deviation.
5.  Runs inference using the TensorFlow Lite interpreter.
6.  Displays the predicted wind speed in knots.
7.  Derives an intensity category using the IMD wind-speed thresholds.

The current demonstration uses a bundled historical sample rather than
live satellite data.

### Planned Emergency Communication

A major proposed feature is offline emergency communication using
Bluetooth Low Energy (BLE).

The planned functionality includes:

-   Offline SOS message exchange.
-   Bluetooth-based store-and-forward communication.
-   Emergency message prioritization.
-   Decentralized sharing of resource availability.
-   Coordination of food, drinking water, medical assistance, and
    shelter requirements.

An additional proposed feature is on-device SOS message classification
using a 1D-CNN model converted to TensorFlow Lite.

Messages will be classified into three priority levels:

-   **Critical:** Search and rescue, medical assistance, or
    death-related messages.
-   **High:** Requests involving food, water, or shelter.
-   **Low:** Other messages.

These emergency communication and classification features are part of
the proposed system design and are not yet implemented in the current
Android codebase.

------------------------------------------------------------------------

## 6. Backend

The repository includes a FastAPI backend prototype.

Current API endpoints:

<table>
  <thead>
    <tr>
      <th>Method</th>
      <th>Endpoint</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>GET</td>
      <td><code>/health</code></td>
      <td>Check backend availability</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/storms</code></td>
      <td>Retrieve storm records with optional year and basin filters</td>
    </tr>
    <tr>
      <td>GET</td>
      <td><code>/storms/{sid}/timeline</code></td>
      <td>Retrieve a storm's timeline</td>
    </tr>
  </tbody>
</table>

The current backend uses sample in-memory storm records and timelines.
Integration with the distributed processing pipeline and trained models
remains future work.

------------------------------------------------------------------------

## 7. Technology Stack

<table>
  <thead>
    <tr>
      <th>Category</th>
      <th>Technologies</th>
    </tr>
  </thead>
  <tbody>
    <tr><td>Programming Languages</td><td>Python, Kotlin</td></tr>
    <tr><td>Big Data</td><td>Hadoop HDFS, Apache Spark, PySpark</td></tr>
    <tr><td>Scientific Computing</td><td>NumPy, Pandas, xarray, netCDF4, PyArrow</td></tr>
    <tr><td>Data Storage</td><td>HDFS, Parquet, MongoDB (planned integration)</td></tr>
    <tr><td>Deep Learning</td><td>TensorFlow, Keras</td></tr>
    <tr><td>Model Architectures</td><td>CNN, ConvLSTM</td></tr>
    <tr><td>Model Deployment</td><td>TensorFlow Lite</td></tr>
    <tr><td>Backend</td><td>Python, FastAPI</td></tr>
    <tr><td>Android Development</td><td>Android Studio, Jetpack Compose</td></tr>
    <tr><td>Build System</td><td>Gradle, Kotlin DSL</td></tr>
    <tr><td>Distributed Environment</td><td>Ubuntu 22.04, WSL2, Tailscale</td></tr>
  </tbody>
</table>

------------------------------------------------------------------------

## 8. Repository Structure

``` text
CycloneConnect/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── assets/
│       │   │   ├── cyclone_cnn.tflite
│       │   │   ├── preprocessing.json
│       │   │   ├── sample_channel1.csv
│       │   │   ├── sample_info.json
│       │   │   └── sample_satellite.png
│       │   │
│       │   ├── java/com/example/cycloneconnect/
│       │   │   ├── MainActivity.kt
│       │   │   ├── AppNavigation.kt
│       │   │   ├── CyclonePredictor.kt
│       │   │   └── ui/
│       │   │       ├── screens/
│       │   │       └── theme/
│       │   │
│       │   └── AndroidManifest.xml
│       │
│       └── test/
│
├── backend/
│   └── app/
│       └── main.py
│
├── big_data/
│   ├── download_gridsat.py
│   ├── step5_ibtracs.py
│   ├── step6_match.py
│   ├── step7_patches.py
│   ├── check_patches.py
│   ├── check_runs.py
│   ├── demo_queries.py
│   └── plot_patches.py
│
├── deep_learning/
│   ├── notebooks/
│   │   ├── 01_CNN_Channel1.ipynb
│   │   ├── 02_ConvLSTM_Channel1.ipynb
│   │   └── 03_Model_Comparison.ipynb
│   │
│   └── src/
│       ├── __init__.py
│       ├── preprocessing.py
│       ├── create_sequences.py
│       ├── model.py
│       ├── train.py
│       └── evaluate.py
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── requirements.txt
└── README.md
```

### Directory Description

<table>
  <thead>
    <tr>
      <th>Directory</th>
      <th>Purpose</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>app/</code></td>
      <td>Android application and on-device CNN inference</td>
    </tr>
    <tr>
      <td><code>backend/</code></td>
      <td>FastAPI backend prototype</td>
    </tr>
    <tr>
      <td><code>big_data/</code></td>
      <td>Satellite acquisition, distributed processing, and dataset preparation scripts</td>
    </tr>
    <tr>
      <td><code>deep_learning/notebooks/</code></td>
      <td>CNN, ConvLSTM, and model comparison experiments</td>
    </tr>
    <tr>
      <td><code>deep_learning/src/</code></td>
      <td>Reusable preprocessing, model building, training, and evaluation modules</td>
    </tr>
    <tr>
      <td><code>gradle/</code></td>
      <td>Android build configuration and dependency management</td>
    </tr>
  </tbody>
</table>

------------------------------------------------------------------------

## 9. Getting Started

### A. Android Application

**Requirements** - Android Studio - Android SDK - JDK 11-compatible
build environment

**Steps**

1.  Clone the repository:

``` bash
git clone https://github.com/<username>/CycloneConnect.git
```

2.  Open the project in Android Studio.
3.  Allow Gradle to synchronize dependencies.
4.  Connect an Android device or start an emulator.
5.  Run the `app` configuration.

The repository currently includes a sample satellite image and a bundled
TensorFlow Lite CNN model for the experimental prediction demonstration.

### B. Deep Learning Experiments

**Requirements** - Python 3 - Google Colab or a compatible local Python
environment - Access to the prepared satellite datasets

Install the Python dependencies:

``` bash
pip install -r requirements.txt
```

Run the notebooks in the following order:

``` text
01_CNN_Channel1.ipynb
02_ConvLSTM_Channel1.ipynb
03_Model_Comparison.ipynb
```

The notebooks require the prepared datasets generated by the Big Data
processing pipeline.

### C. Backend

Install FastAPI and Uvicorn:

``` bash
pip install fastapi uvicorn
```

Navigate to the repository root and start the backend:

``` bash
uvicorn backend.app.main:app --reload
```

Access the API documentation at:

``` text
http://127.0.0.1:8000/docs
```

Check backend availability:

``` text
http://127.0.0.1:8000/health
```

### D. Big Data Pipeline

The Big Data scripts are designed to run in a configured Hadoop and
Spark environment.

The intended workflow is:

1.  Configure the two-node Hadoop HDFS and Spark cluster.
2.  Download and ingest GridSat-B1 imagery.
3.  Ingest and clean IBTrACS records.
4.  Match cyclone records with satellite timestamps.
5.  Extract storm-centred image patches.
6.  Store processed datasets in HDFS.

The scripts use predefined HDFS directories under `/project/`. Configure
the cluster and required paths before executing them.

------------------------------------------------------------------------

## 10. Research Gaps Addressed

The project is motivated by the following research and implementation
gaps:

<table>
  <thead>
    <tr>
      <th>Identified Gap</th>
      <th>Proposed Approach</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Disconnect between cyclone forecasting and disaster response</td>
      <td>Integrate cyclone intelligence and emergency coordination in one application</td>
    </tr>
    <tr>
      <td>Dependence on cellular networks and internet</td>
      <td>BLE-based offline communication</td>
    </tr>
    <tr>
      <td>Lack of decentralized resource coordination</td>
      <td>Offline sharing of emergency requirements and available resources</td>
    </tr>
    <tr>
      <td>Computational challenges in deep learning</td>
      <td>Distributed data processing and cloud-based training</td>
    </tr>
    <tr>
      <td>Difficulty prioritizing emergency messages</td>
      <td>On-device NLP-based SOS classification</td>
    </tr>
  </tbody>
</table>

------------------------------------------------------------------------

## 11. Future Scope

-   Integrate live satellite data acquisition.
-   Extend the prediction system to support more cyclone observations
    and geographical regions.
-   Explore additional satellite channels and temporal sequence lengths.
-   Improve wind-speed prediction performance.
-   Integrate the trained ConvLSTM model with the backend.
-   Develop a complete cyclone tracking and visualization interface.
-   Implement BLE-based offline SOS communication.
-   Implement on-device emergency message classification.
-   Develop decentralized emergency resource coordination.
-   Integrate official cyclone bulletins and alerts.

------------------------------------------------------------------------

## 12. Disclaimer

CycloneConnect is an academic research prototype developed to explore
the application of Big Data Analytics and Deep Learning in cyclone
intelligence and disaster management.

Its experimental predictions are not substitutes for official
meteorological forecasts, warnings, or evacuation instructions.

For operational cyclone warnings, always follow official advisories
issued by the India Meteorological Department (IMD) and relevant
disaster-management authorities.

------------------------------------------------------------------------

## 13. Contributors

**Project Team**

-   Hema Shree G
-   Sinchana B M

**Institution:** BMS College of Engineering

**Project Focus:** Big Data Analytics, Deep Learning, and Android
Application Development.
