require('dotenv').config();

const express = require('express');
const { Pool } = require('pg');
const jwt = require('jsonwebtoken');
const nodemailer = require('nodemailer');
const moment = require('moment');
// const cors = require('cors');

const app = express();
const port = process.env.PORT || 3000;

// app.use(cors());

app.use(express.json());

const pool = new Pool({
  host: process.env.DB_HOST,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  port: 5432,
  ssl: {
    rejectUnauthorized: false
  }
});

const transporter = nodemailer.createTransport({
  host: process.env.EMAIL_HOST,
  port: parseInt(process.env.EMAIL_PORT) || 587,
  secure: false,  // true, если порт 465
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS
  }
});

app.get('/', (req, res) => {
  res.send('Server is running');
});


app.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    const findUserQuery = 'SELECT * FROM client WHERE email = $1';
    const result = await pool.query(findUserQuery, [email]);

    if (result.rows.length === 0) {
      return res.status(401).json({ success: false, message: 'User not found' });
    }

    const user = result.rows[0];

    if (password !== user.password) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    const token = jwt.sign(
      { userId: user.id, email: user.email },
      process.env.JWT_SECRET,
      { expiresIn: '1h' } // токен живёт 1 час
    );

    return res.json({
      success: true,
      message: 'Login successful',
      token: token,
      userId: user.id
    });
  } catch (error) {
    console.error('Error in /login:', error);
    return res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
});



app.post('/register', async (req, res) => {
  try {
    const { email, password, name, number } = req.body;

    const checkUserQuery = 'SELECT * FROM client WHERE email = $1';
    const existingUser = await pool.query(checkUserQuery, [email]);
    if (existingUser.rows.length > 0) {
      return res.status(400).json({ success: false, message: 'Email already exists' });
    }

    const insertUserQuery = `
      INSERT INTO client (email, password)
      VALUES ($1, $2)
      RETURNING id
    `;
    const newUser = await pool.query(insertUserQuery, [email, password]);
    const userId = newUser.rows[0].id; // Получаем сгенерированный id

    const insertProfileQuery = `
      INSERT INTO profileinformation ("Location", "Name", phonenumber, profile_id)
      VALUES ($1, $2, $3, $4)
    `;
    await pool.query(insertProfileQuery, ['Almaty', name, number, userId]);

    return res.json({
      success: true,
      message: 'User registered',
      userId: userId
    });
  } catch (error) {
    console.error('Error in /register:', error);
    return res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
});


// (forgot-password) ===========
app.post('/forgot-password', async (req, res) => {
  try {
    const { email } = req.body;

    const userQuery = 'SELECT id FROM client WHERE email = $1';
    const userResult = await pool.query(userQuery, [email]);

    if (userResult.rows.length === 0) {
      return res
        .status(404)
        .json({ success: false, message: 'User with this email not found' });
    }

    const userId = userResult.rows[0].id;

    const resetCode = Math.floor(1000 + Math.random() * 9000).toString();

    const expiresAt = new Date(Date.now() + 15 * 60 * 1000);

    await pool.query('DELETE FROM password_resets WHERE user_id = $1', [userId]);

    const insertQuery = `
      INSERT INTO password_resets (user_id, reset_code, expires_at, used)
      VALUES ($1, $2, $3, $4)
    `;
    await pool.query(insertQuery, [userId, resetCode, expiresAt, false]);

    const mailOptions = {
      from: process.env.EMAIL_USER,
      to: email,
      subject: 'Password Reset Code',
      text: `Ваш код для сброса пароля: ${resetCode}. Код действителен 15 минут.`
    };
    await transporter.sendMail(mailOptions);

    return res.json({
      success: true,
      message: 'Reset code sent to email'
    });
  } catch (error) {
    console.error('Error in /forgot-password:', error);
    return res.status(500).json({ success: false, message: 'Server error' });
  }
});

app.post('/reset-password', async (req, res) => {
  try {
    const { email, newPassword } = req.body;

    const userResult = await pool.query('SELECT id FROM client WHERE email=$1', [email]);
    if (userResult.rows.length === 0) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    const userId = userResult.rows[0].id;

    await pool.query('UPDATE client SET password=$1 WHERE id=$2', [newPassword, userId]);

    return res.json({ success: true, message: 'Password has been reset' });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Server error' });
  }
});


//Verify OTP Code in SERVER
app.post('/verify-code', async (req, res) => {
  try {
    const { email, code } = req.body;

    if (!email || !code) {
      return res.status(400).json({ success: false, message: "Missing email or code" });
    }

    const userQuery = 'SELECT id FROM client WHERE email = $1';
    const userResult = await pool.query(userQuery, [email]);
    if (userResult.rows.length === 0) {
      return res.status(404).json({ success: false, message: "User not found" });
    }
    const userId = userResult.rows[0].id;

    const resetQuery = `
      SELECT id, reset_code, expires_at, used
      FROM password_resets
      WHERE user_id = $1
      ORDER BY id DESC
      LIMIT 1
    `;
    const resetResult = await pool.query(resetQuery, [userId]);
    if (resetResult.rows.length === 0) {
      return res.status(400).json({ success: false, message: "No reset request found" });
    }
    const resetRecord = resetResult.rows[0];

    if (resetRecord.used) {
      return res.status(400).json({ success: false, message: "Code already used" });
    }

    if (new Date() > new Date(resetRecord.expires_at)) {
      return res.status(400).json({ success: false, message: "Code has expired" });
    }

    if (resetRecord.reset_code !== code) {
      return res.status(400).json({ success: false, message: "Invalid code" });
    }

    const updateQuery = 'UPDATE password_resets SET used = true WHERE id = $1';
    await pool.query(updateQuery, [resetRecord.id]);

    return res.json({ success: true, message: "Code verified" });
  } catch (error) {
    console.error('Error in /verify-code:', error);
    return res.status(500).json({ success: false, message: "Server error" });
  }
});

app.get('/profile/:id', async (req, res) => {
  try {
    const profileId = req.params.id;
    const query = `
      SELECT "Name" as name, phonenumber, "Location" as location
      FROM profileinformation
      WHERE profile_id = $1
    `;
    const result = await pool.query(query, [profileId]);

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Profile not found' });
    }

    res.json({ success: true, profile: result.rows[0] });
  } catch (error) {
    console.error('Error in /profile/:id:', error);
    res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
});

app.get('/document-details/:clientId', async (req, res) => {
  try {
    const clientId = req.params.clientId;
    const query = `
      SELECT
          d.name as doctor_name,
          encode(doc.doc_data, 'base64') as doc_data,
          doc.doc_type,
          TO_CHAR(doc.created_at, 'DD.MM.YYYY HH24:MI') as document_date
      FROM
          documents doc
      JOIN
          client c ON c.id = doc.client_id
      JOIN
          doctors d ON d.id = doc.doctor_id
      WHERE
          doc.client_id = $1

    `;

    const result = await pool.query(query, [clientId]);

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Documents not found' });
    }

    const documents = result.rows.map(row => {
      const base64data = row.doc_data.toString('base64');

      return {
        doctor_name: row.doctor_name,
        doc_type: row.doc_type,
        document_date: row.document_date,
        base64data: base64data // возвращаем строку base64
      };
    });

    // Ответ с данными о документах
    res.json({ success: true, documents: documents });
  } catch (error) {
    console.error('Error in /document-details/:clientId:', error);
    res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
});



app.put('/profile/:id/number', async (req, res) => {
  try {
    const userId = req.params.id;
    const { number } = req.body;

    const updateProfileQuery = `
      UPDATE profileinformation
      SET phonenumber = $1
      WHERE profile_id = $2
    `;
    await pool.query(updateProfileQuery, [number, userId]);

    return res.json({
      success: true,
      message: 'Contact number updated successfully'
    });
  } catch (error) {
    console.error('Error in /profile/:id/number:', error);
    return res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
});

app.put('/profile/:id/name', async (req, res) => {
  try {
    const userId = req.params.id;
    const { name } = req.body;

    const updateProfileQuery = `
      UPDATE profileinformation
      SET "Name" = $1
      WHERE profile_id = $2
    `;
    await pool.query(updateProfileQuery, [name, userId]);

    return res.json({
      success: true,
      message: 'Name updated successfully'
    });
  } catch (error) {
    console.error('Error in /profile/:id/name:', error);
    return res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
});

app.put('/client/:id/password', async (req, res) => {
  try {
    const userId = req.params.id;
    const { password } = req.body;

    const updatePasswordQuery = `
      UPDATE client
      SET password = $1
      WHERE id = $2
    `;
    await pool.query(updatePasswordQuery, [password, userId]);

    return res.json({
      success: true,
      message: 'Password updated successfully'
    });
  } catch (error) {
    console.error('Error in /client/:id/password:', error);
    return res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
});

app.get('/doctors', async (req, res) => {
  try {
    const query = `
      SELECT
          d.id,
          d.name AS "Name Surname",
          d.phone,
          string_agg(DISTINCT s.name, ', ') AS "Specialities",
          string_agg(
              DISTINCT ds.name_service || ' (' || ds.price || ')',
              ', '
          ) AS "Services"
      FROM doctors d
      LEFT JOIN doctor_specialties dspec ON d.id = dspec.doctor_id
      LEFT JOIN specialties s ON dspec.specialty_id = s.id
      LEFT JOIN doctor_services ds ON d.id = ds.doctor_id
      GROUP BY d.id, d.name, d.phone
      ORDER BY d.id;
    `;

    const result = await pool.query(query);
    res.json({ success: true, doctors: result.rows });
  } catch (error) {
    console.error('Error fetching doctors:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

app.get('/specialties', async (req, res) => {
  try {
    const query = 'SELECT id, name FROM specialties ORDER BY name';
    const result = await pool.query(query);
    res.json({ success: true, specialties: result.rows });
  } catch (error) {
    console.error('Error fetching specialties:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

app.get('/doctors-by-specialty/:specialtyId', async (req, res) => {
  const specialtyId = req.params.specialtyId;
  try {
    const query = `
      SELECT
          d.id,
          d.name,
          d.phone,
          s.name as specialty,
          string_agg(DISTINCT ds1.name_service || ' (' || ds1.price || ')', ', ') AS services
      FROM doctors d
      JOIN doctor_specialties ds2 ON d.id = ds2.doctor_id
      JOIN specialties s ON ds2.specialty_id = s.id
      LEFT JOIN doctor_services ds1 ON d.id = ds1.doctor_id  -- Используем другой алиас для doctor_services
      WHERE s.id = $1
      GROUP BY d.id, d.name, d.phone, s.name
      ORDER BY d.id;
    `;

    const result = await pool.query(query, [specialtyId]);
    res.json({ success: true, doctors: result.rows });
  } catch (error) {
    console.error('Error fetching doctors:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});


app.post('/create-appointment', async (req, res) => {
  try {
    const { user_id, doctor_id, appointment_date, appointment_time, phone, reason, status = 'Pending' } = req.body;

    if (!user_id || !doctor_id || !appointment_date || !appointment_time) {
      return res.status(400).json({ success: false, message: 'Missing required fields' });
    }

    const formattedDate = moment(appointment_date, 'D/M/YYYY').format('YYYY-MM-DD');
    console.log("Formatted appointment_date:", formattedDate);

    const query = `
      INSERT INTO appointments (user_id, doctor_id, appointment_date, appointment_time, phone, reason, status, created_at)
      VALUES ($1, $2, $3, $4, $5, $6, $7, NOW())
      RETURNING id, user_id, doctor_id, appointment_date, appointment_time, phone, reason, status, created_at;
    `;

    const values = [user_id, doctor_id, formattedDate, appointment_time, phone, reason, status];

    const result = await pool.query(query, values);

    console.log("New appointment created:", result.rows)
    const newAppointment = result.rows[0];
    res.json({ success: true, appointment: newAppointment });
  } catch (error) {
    console.error('Error creating appointment:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

app.get('/appointments/:id', async (req, res) => {
  const userId = req.params.id;

  if (!userId) {
    return res.status(400).json({
      success: false,
      message: 'User ID is required'
    });
  }

  try {
    const query = `
      SELECT
          a.id AS appointment_id,
          a.user_id,
          a.doctor_id,
          a.appointment_date,
          a.appointment_time,
          a.phone AS appointment_phone,
          a.reason AS appointment_reason,
          a.status AS appointment_status,
          a.created_at AS appointment_created_at,
          d.name AS doctor_name,
          d.phone AS doctor_phone,
          d.email AS doctor_email,
          d.created_at AS doctor_created_at
      FROM
          appointments a
      JOIN
          doctors d ON a.doctor_id = d.id
      WHERE
          a.user_id = $1;
    `;

    const result = await pool.query(query, [userId]);

    if (result.rows.length > 0) {
      res.status(200).json({
        success: true,
        appointments: result.rows
      });
    } else {
      res.status(404).json({
        success: false,
        message: 'No appointments found for this user'
      });
    }
  } catch (error) {
    console.error('Error retrieving appointments:', error);
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

app.post('/delete-appointment', async (req, res) => {
  try {
    const { appointment_id } = req.body;

    if (!appointment_id) {
      return res.status(400).json({ success: false, message: 'Appointment ID is required' });
    }

    const query = `
      DELETE FROM appointments
      WHERE id = $1
      RETURNING id;
    `;

    const values = [appointment_id];

    const result = await pool.query(query, values);

    if (result.rowCount > 0) {
      res.json({ success: true, message: `Appointment with ID ${appointment_id} has been deleted.` });
    } else {
      res.status(404).json({ success: false, message: 'Appointment not found' });
    }
  } catch (error) {
    console.error('Error deleting appointment:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

app.post('/reschedule-appointment', async (req, res) => {
  try {
    const { appointment_id, new_date, new_time, reason_to_visit } = req.body;

    if (!appointment_id || !new_date || !new_time) {
      return res.status(400).json({ success: false, message: 'Missing required fields' });
    }

    const formattedDate = moment(new_date, 'D/M/YYYY').format('YYYY-MM-DD');
    console.log("Formatted new appointment date:", formattedDate);

    let query = `
      UPDATE appointments
      SET appointment_date = $1, appointment_time = $2, created_at = NOW(), reason = $3
      WHERE id = $4
      RETURNING id, appointment_date, appointment_time, reason;
    `;

    const values = [formattedDate, new_time, reason_to_visit, appointment_id];

    const result = await pool.query(query, values);

    if (result.rowCount > 0) {
      const updatedAppointment = result.rows[0];
      res.json({
        success: true,
        message: `Appointment with ID ${updatedAppointment.appointment_id} has been rescheduled.`,
        appointment: updatedAppointment
      });
    } else {
      res.status(404).json({ success: false, message: 'Appointment not found' });
    }
  } catch (error) {
    console.error('Error rescheduling appointment:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

// Run server
app.listen(port, () => {
  console.log(`Server is running on http://0.0.0.0:${port}`);
});
