-- =============================================
-- SYMPTOMS
-- =============================================
CREATE TABLE symptoms (
    id       BIGSERIAL    PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    icon     VARCHAR(50)  NOT NULL DEFAULT 'fas-circle',
    category VARCHAR(50)  NOT NULL DEFAULT 'General'
);

-- =============================================
-- ALLERGIES
-- =============================================
CREATE TABLE allergies (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    icon VARCHAR(50)  NOT NULL DEFAULT 'fas-exclamation-triangle'
);

-- =============================================
-- DISEASES
-- =============================================
CREATE TABLE diseases (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    icd_code    VARCHAR(10),
    description TEXT
);

CREATE TABLE disease_specializations (
    disease_id        BIGINT NOT NULL REFERENCES diseases(id)        ON DELETE CASCADE,
    specialization_id BIGINT NOT NULL REFERENCES specializations(id) ON DELETE CASCADE,
    PRIMARY KEY (disease_id, specialization_id)
);

-- =============================================
-- JUNCTION TABLES
-- =============================================
CREATE TABLE appointment_symptoms (
    appointment_id BIGINT NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    symptom_id     BIGINT NOT NULL REFERENCES symptoms(id)     ON DELETE CASCADE,
    PRIMARY KEY (appointment_id, symptom_id)
);

CREATE TABLE patient_allergies (
    patient_id BIGINT NOT NULL REFERENCES patients(id)  ON DELETE CASCADE,
    allergy_id BIGINT NOT NULL REFERENCES allergies(id) ON DELETE CASCADE,
    PRIMARY KEY (patient_id, allergy_id)
);

ALTER TABLE patients DROP COLUMN IF EXISTS allergies;

-- =============================================
-- SEED: SYMPTOMS (все иконки проверены по kordamp cheat-sheet)
-- =============================================
INSERT INTO symptoms (name, icon, category) VALUES
-- General
('Fever',              'fas-thermometer-half',      'General'),
('Fatigue',            'fas-bed',                   'General'),
('Weakness',           'fas-tired',                 'General'),
('Night sweats',       'fas-tint',                  'General'),
('Weight loss',        'fas-weight',                'General'),
('Weight gain',        'fas-weight',                'General'),
('Loss of appetite',   'fas-utensil-spoon',         'General'),
('Chills',             'fas-snowflake',             'General'),
('Malaise',            'fas-frown',                 'General'),

-- Respiratory
('Cough',              'fas-head-side-cough',       'Respiratory'),
('Dry cough',          'fas-head-side-cough',       'Respiratory'),
('Wet cough',          'fas-head-side-cough',       'Respiratory'),
('Shortness of breath','fas-lungs',                 'Respiratory'),
('Wheezing',           'fas-lungs-virus',           'Respiratory'),
('Sore throat',        'fas-head-side-virus',       'Respiratory'),
('Runny nose',         'fas-head-side-mask',        'Respiratory'),
('Nasal congestion',   'fas-head-side-mask',        'Respiratory'),
('Sneezing',           'fas-head-side-cough',       'Respiratory'),
('Hoarseness',         'fas-microphone-slash',      'Respiratory'),

-- Cardiovascular
('Chest pain',         'fas-heartbeat',             'Cardiovascular'),
('Palpitations',       'fas-heart',                 'Cardiovascular'),
('Rapid heartbeat',    'fas-heartbeat',             'Cardiovascular'),
('Irregular heartbeat','fas-heart-broken',          'Cardiovascular'),
('Swollen ankles',     'fas-shoe-prints',           'Cardiovascular'),
('Dizziness',          'fas-dizzy',                 'Cardiovascular'),
('Fainting',           'fas-user-injured',          'Cardiovascular'),

-- Neurological
('Headache',           'fas-brain',                 'Neurological'),
('Migraine',           'fas-brain',                 'Neurological'),
('Numbness',           'fas-hand-paper',            'Neurological'),
('Tingling',           'fas-hand-sparkles',         'Neurological'),
('Tremor',             'fas-hand-paper',            'Neurological'),
('Memory loss',        'fas-brain',                 'Neurological'),
('Confusion',          'fas-head-side-virus',       'Neurological'),
('Insomnia',           'fas-moon',                  'Neurological'),

-- Gastrointestinal
('Nausea',             'fas-meh-rolling-eyes',      'Gastrointestinal'),
('Vomiting',           'fas-procedures',            'Gastrointestinal'),
('Diarrhea',           'fas-toilet',                'Gastrointestinal'),
('Constipation',       'fas-toilet',                'Gastrointestinal'),
('Abdominal pain',     'fas-stethoscope',           'Gastrointestinal'),
('Bloating',           'fas-circle',                'Gastrointestinal'),
('Heartburn',          'fas-fire',                  'Gastrointestinal'),
('Blood in stool',     'fas-tint',                  'Gastrointestinal'),

-- Musculoskeletal
('Back pain',          'fas-bone',                  'Musculoskeletal'),
('Joint pain',         'fas-bone',                  'Musculoskeletal'),
('Muscle pain',        'fas-dumbbell',              'Musculoskeletal'),
('Stiffness',          'fas-crutch',                'Musculoskeletal'),
('Swollen joints',     'fas-bone',                  'Musculoskeletal'),
('Neck pain',          'fas-bone',                  'Musculoskeletal'),

-- Skin
('Rash',               'fas-allergies',             'Skin'),
('Itching',            'fas-hand-paper',            'Skin'),
('Hives',              'fas-allergies',             'Skin'),
('Dry skin',           'fas-tint-slash',            'Skin'),
('Bruising',           'fas-band-aid',              'Skin'),
('Jaundice',           'fas-sun',                   'Skin'),
('Pallor',             'fas-circle',                'Skin'),

-- ENT
('Ear pain',           'fas-deaf',                  'ENT'),
('Hearing loss',       'fas-deaf',                  'ENT'),
('Tinnitus',           'fas-assistive-listening-systems', 'ENT'),
('Blurred vision',     'fas-low-vision',            'ENT'),
('Eye redness',        'fas-eye',                   'ENT'),
('Nosebleed',          'fas-tint',                  'ENT'),
('Toothache',          'fas-tooth',                 'ENT'),

-- Urological
('Frequent urination', 'fas-toilet',                'Urological'),
('Painful urination',  'fas-toilet',                'Urological'),
('Blood in urine',     'fas-tint',                  'Urological'),

-- Psychological
('Anxiety',            'fas-brain',                 'Psychological'),
('Depression',         'fas-sad-tear',              'Psychological'),
('Mood swings',        'fas-theater-masks',         'Psychological'),
('Irritability',       'fas-angry',                 'Psychological');

-- =============================================
-- SEED: ALLERGIES
-- =============================================
INSERT INTO allergies (name, icon) VALUES
-- Food
('Peanuts',          'fas-allergies'),
('Tree nuts',        'fas-allergies'),
('Milk',             'fas-allergies'),
('Eggs',             'fas-egg'),
('Wheat / Gluten',   'fas-bread-slice'),
('Soy',              'fas-seedling'),
('Fish',             'fas-fish'),
('Shellfish',        'fas-fish'),
('Sesame',           'fas-seedling'),
('Mustard',          'fas-seedling'),
-- Medications
('Penicillin',       'fas-pills'),
('Aspirin',          'fas-pills'),
('NSAIDs',           'fas-pills'),
('Sulfonamides',     'fas-pills'),
('Codeine',          'fas-prescription-bottle'),
('Local anesthetics','fas-syringe'),
('Contrast dye',     'fas-vial'),
-- Environmental
('Pollen',           'fas-leaf'),
('Dust mites',       'fas-home'),
('Mold',             'fas-bacterium'),
('Pet dander',       'fas-paw'),
('Latex',            'fas-band-aid'),
('Insect stings',    'fas-bug'),
('Nickel',           'fas-ring'),
('Sunlight',         'fas-sun'),
-- Other
('Alcohol',          'fas-wine-glass'),
('Caffeine',         'fas-mug-hot');

-- =============================================
-- SEED: DISEASES
-- =============================================
INSERT INTO diseases (name, icd_code, description) VALUES
('Hypertension',                'I10',    'High blood pressure'),
('Type 2 Diabetes',             'E11',    'Metabolic disorder with high blood sugar'),
('Upper respiratory infection', 'J06.9',  'Common cold and similar infections'),
('Influenza',                   'J11',    'Viral respiratory illness'),
('Pneumonia',                   'J18',    'Lung infection'),
('Anemia',                      'D64',    'Low red blood cell count'),
('Hypothyroidism',              'E03',    'Underactive thyroid'),
('Obesity',                     'E66',    'Excess body fat'),
('Depression',                  'F32',    'Major depressive disorder'),
('Anxiety disorder',            'F41',    'Generalised anxiety'),
('Coronary artery disease',     'I25',    'Narrowing of heart arteries'),
('Heart failure',               'I50',    'Heart unable to pump efficiently'),
('Atrial fibrillation',         'I48',    'Irregular heart rhythm'),
('Myocardial infarction',       'I21',    'Heart attack'),
('Angina pectoris',             'I20',    'Chest pain from reduced blood flow'),
('Arrhythmia',                  'I49',    'Abnormal heart rhythm'),
('Cardiomyopathy',              'I42',    'Disease of heart muscle'),
('Pericarditis',                'I30',    'Inflammation of heart lining'),
('Acute otitis media',          'H66',    'Middle ear infection'),
('Chronic sinusitis',           'J32',    'Long-term sinus inflammation'),
('Tonsillitis',                 'J35',    'Inflammation of tonsils'),
('Laryngitis',                  'J37',    'Inflammation of larynx'),
('Allergic rhinitis',           'J30',    'Hay fever'),
('Vertigo',                     'H81',    'Balance disorder'),
('Epilepsy',                    'G40',    'Seizure disorder'),
('Multiple sclerosis',          'G35',    'Autoimmune demyelinating disease'),
('Parkinsons disease',          'G20',    'Progressive neurological disorder'),
('Stroke',                      'I63',    'Brain ischemia'),
('Tension headache',            'G44',    'Most common headache type'),
('Gastroesophageal reflux',     'K21',    'Acid reflux disease'),
('Peptic ulcer',                'K27',    'Stomach or duodenal ulcer'),
('Irritable bowel syndrome',    'K58',    'Functional bowel disorder'),
('Crohns disease',              'K50',    'Inflammatory bowel disease'),
('Ulcerative colitis',          'K51',    'Colon inflammation'),
('Celiac disease',              'K90.0',  'Gluten intolerance'),
('Osteoarthritis',              'M19',    'Degenerative joint disease'),
('Rheumatoid arthritis',        'M06',    'Autoimmune joint disease'),
('Osteoporosis',                'M81',    'Low bone density'),
('Herniated disc',              'M51',    'Spinal disc protrusion'),
('Tendinitis',                  'M77',    'Tendon inflammation'),
('Fracture',                    'S00',    'Broken bone');

-- =============================================
-- DISEASE <-> SPECIALIZATION LINKS (по ILIKE)
-- =============================================
-- Update specialization names to English
UPDATE specializations SET name = 'General Practitioner' WHERE name ILIKE '%терапевт%';
UPDATE specializations SET name = 'Cardiologist'         WHERE name ILIKE '%кардиолог%';
UPDATE specializations SET name = 'ENT Specialist'       WHERE name ILIKE '%лор%';
UPDATE specializations SET name = 'Neurologist'          WHERE name ILIKE '%невро%';
UPDATE specializations SET name = 'Gastroenterologist'   WHERE name ILIKE '%гастро%';
UPDATE specializations SET name = 'Orthopedist'          WHERE name ILIKE '%ортопед%';
UPDATE specializations SET name = 'Dermatologist'        WHERE name ILIKE '%дермато%';
UPDATE specializations SET name = 'Ophthalmologist'      WHERE name ILIKE '%офтальмо%';
UPDATE specializations SET name = 'Urologist'            WHERE name ILIKE '%уролог%';
UPDATE specializations SET name = 'Endocrinologist'      WHERE name ILIKE '%эндокрино%';
UPDATE specializations SET name = 'Pulmonologist'        WHERE name ILIKE '%пульмоно%';
UPDATE specializations SET name = 'Rheumatologist'       WHERE name ILIKE '%ревмато%';
UPDATE specializations SET name = 'Psychiatrist'         WHERE name ILIKE '%психиатр%';
UPDATE specializations SET name = 'Surgeon'              WHERE name ILIKE '%хирург%';
UPDATE specializations SET name = 'Pediatrician'         WHERE name ILIKE '%педиатр%';

INSERT INTO disease_specializations (disease_id, specialization_id)
SELECT d.id, s.id FROM diseases d, specializations s
WHERE s.name ILIKE '%терапевт%'
AND d.name IN (
    'Hypertension','Type 2 Diabetes','Upper respiratory infection',
    'Influenza','Pneumonia','Anemia','Hypothyroidism',
    'Obesity','Depression','Anxiety disorder'
);

INSERT INTO disease_specializations (disease_id, specialization_id)
SELECT d.id, s.id FROM diseases d, specializations s
WHERE s.name ILIKE '%кардиолог%'
AND d.name IN (
    'Coronary artery disease','Heart failure','Atrial fibrillation',
    'Myocardial infarction','Angina pectoris','Arrhythmia',
    'Cardiomyopathy','Pericarditis','Hypertension'
);

INSERT INTO disease_specializations (disease_id, specialization_id)
SELECT d.id, s.id FROM diseases d, specializations s
WHERE s.name ILIKE '%лор%'
AND d.name IN (
    'Acute otitis media','Chronic sinusitis','Tonsillitis',
    'Laryngitis','Allergic rhinitis','Vertigo'
);

INSERT INTO disease_specializations (disease_id, specialization_id)
SELECT d.id, s.id FROM diseases d, specializations s
WHERE s.name ILIKE '%невро%'
AND d.name IN (
    'Epilepsy','Multiple sclerosis','Parkinsons disease',
    'Stroke','Tension headache'
);

INSERT INTO disease_specializations (disease_id, specialization_id)
SELECT d.id, s.id FROM diseases d, specializations s
WHERE s.name ILIKE '%гастро%'
AND d.name IN (
    'Gastroesophageal reflux','Peptic ulcer','Irritable bowel syndrome',
    'Crohns disease','Ulcerative colitis','Celiac disease'
);

INSERT INTO disease_specializations (disease_id, specialization_id)
SELECT d.id, s.id FROM diseases d, specializations s
WHERE s.name ILIKE '%ортопед%'
AND d.name IN (
    'Osteoarthritis','Rheumatoid arthritis','Osteoporosis',
    'Herniated disc','Tendinitis','Fracture'
);