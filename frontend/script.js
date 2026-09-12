const API = "/api";

let token = localStorage.getItem("bidyatra_token");

let currentUser = JSON.parse(
    localStorage.getItem("bidyatra_user") || "null"
);

let activities = [];

let pendingAdmin = null;


/* =========================================================
   BASIC HELPERS
========================================================= */

const $ = id => document.getElementById(id);

function today() {

    const d = new Date();

    const year = d.getFullYear();

    const month =
        String(d.getMonth() + 1).padStart(2, "0");

    const day =
        String(d.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}


/* =========================================================
   AUTH HEADERS
========================================================= */

function authHeaders() {

    const headers = {
        "Content-Type": "application/json"
    };

    if (token) {

        headers["Authorization"] =
            "Bearer " + token;
    }

    return headers;
}


/* =========================================================
   API HELPER
========================================================= */

async function api(path, options = {}) {

    const config = {

        ...options,

        headers: {

            ...authHeaders(),

            ...(options.headers || {})
        }
    };


    const response =
        await fetch(API + path, config);


    /* =====================================================
       UNAUTHORIZED
    ===================================================== */

    if (response.status === 401) {

        if (token) {

            logout();
        }

        throw new Error(
            "Unauthorized"
        );
    }


    /* =====================================================
       ERROR
    ===================================================== */

    if (!response.ok) {

        let message =
            `Request failed (${response.status})`;


        try {

            const text =
                await response.text();


            if (text) {

                try {

                    const json =
                        JSON.parse(text);


                    message =
                        json.message ||
                        json.error ||
                        message;

                }

                catch {

                    message =
                        text;
                }
            }

        }

        catch {
            // Ignore
        }


        throw new Error(message);
    }


    /* =====================================================
       SUCCESS
    ===================================================== */

    const contentType =
        response.headers.get(
            "content-type"
        ) || "";


    if (
        contentType.includes(
            "application/json"
        )
    ) {

        return response.json();
    }


    return response;
}


/* =========================================================
   TOAST
========================================================= */

function toast(message) {

    const element =
        $("toast");


    if (!element) return;


    element.textContent =
        message;


    element.classList.add(
        "show"
    );


    setTimeout(
        () => {

            element.classList.remove(
                "show"
            );

        },
        2500
    );
}


/* =========================================================
   ROLE BASED UI
========================================================= */

function applyRoleBasedUI() {

    if (!currentUser) {
        return;
    }


    const isAdmin =
        String(
            currentUser.role || ""
        ).toUpperCase() === "ADMIN";


    /* =====================================================
       GET ELEMENTS
    ===================================================== */

    const dashboardNav =
        document.querySelector(
            'a[href="#dashboard"]'
        );


    const activityNav =
        document.querySelector(
            'a[href="#activitySection"]'
        );


    const recordsNav =
        document.querySelector(
            'a[href="#recordsSection"]'
        );


    const usersNav =
        document.querySelector(
            'a[href="#adminUsersSection"]'
        );


    const reportsNav =
        document.querySelector(
            'a[href="#reportsSection"]'
        );


    const hero =
        document.querySelector(
            ".hero"
        );


    const stats =
        document.querySelector(
            ".stats"
        );


    const recordsSection =
        $("recordsSection");


    const adminUsersSection =
        $("adminUsersSection");


    const activitySection =
        $("activitySection");


    const reportsSection =
        $("reportsSection");


    /* =====================================================
       STAFF
    ===================================================== */

    if (!isAdmin) {

        /*
         * STAFF CAN SEE ONLY:
         *
         * 1. Add Daily Activity
         * 2. Reports
         */


        /* -----------------------------------------------
           HIDE DASHBOARD NAV
        ------------------------------------------------ */

        if (dashboardNav) {

            dashboardNav.style.display =
                "none";
        }


        /* -----------------------------------------------
           SHOW ACTIVITY NAV
        ------------------------------------------------ */

        if (activityNav) {

            activityNav.style.display =
                "block";
        }


        /* -----------------------------------------------
           HIDE ACTIVITY HISTORY NAV
        ------------------------------------------------ */

        if (recordsNav) {

            recordsNav.style.display =
                "none";
        }


        /* -----------------------------------------------
           HIDE STAFF ACCOUNTS NAV
        ------------------------------------------------ */

        if (usersNav) {

            usersNav.style.display =
                "none";
        }


        /* -----------------------------------------------
           SHOW REPORT NAV
        ------------------------------------------------ */

        if (reportsNav) {

            reportsNav.style.display =
                "block";
        }


        /* -----------------------------------------------
           HIDE HERO
        ------------------------------------------------ */

        if (hero) {

            hero.style.display =
                "none";
        }


        /* -----------------------------------------------
           HIDE STATISTICS
        ------------------------------------------------ */

        if (stats) {

            stats.style.display =
                "none";
        }


        /* -----------------------------------------------
           HIDE ACTIVITY HISTORY
        ------------------------------------------------ */

        if (recordsSection) {

            recordsSection.style.display =
                "none";
        }


        /* -----------------------------------------------
           HIDE ADMIN STAFF ACCOUNTS
        ------------------------------------------------ */

        if (adminUsersSection) {

            adminUsersSection.style.display =
                "none";
        }


        /* -----------------------------------------------
           SHOW ACTIVITY SECTION
        ------------------------------------------------ */

        if (activitySection) {

            activitySection.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW REPORTS SECTION
        ------------------------------------------------ */

        if (reportsSection) {

            reportsSection.style.display =
                "block";
        }


        /* -----------------------------------------------
           REPORT TITLE
        ------------------------------------------------ */

        const reportTitle =
            document.querySelector(
                "#reportsSection h2"
            );


        if (reportTitle) {

            reportTitle.textContent =
                "Export My Reports";
        }


        /* -----------------------------------------------
           ACTIVITY LABEL
        ------------------------------------------------ */

        const activityLabel =
            document.querySelector(
                "#activitySection .card-title span"
            );


        if (activityLabel) {

            activityLabel.textContent =
                "STAFF MANAGEMENT";
        }


        /* -----------------------------------------------
           ACTIVITY HEADING
        ------------------------------------------------ */

        const activityHeading =
            document.querySelector(
                "#activitySection #formTitle"
            );


        if (activityHeading) {

            activityHeading.textContent =
                "Add Daily Activity";
        }


        /* -----------------------------------------------
           MAKE ADD ACTIVITY FIRST SECTION
        ------------------------------------------------ */

        if (activitySection) {

            activitySection.scrollIntoView =
                activitySection.scrollIntoView;
        }


        /*
         * IMPORTANT:
         *
         * If user opens localhost:8080 directly,
         * don't automatically show hidden dashboard.
         */

        if (
            location.hash === "#dashboard" ||
            location.hash === "" ||
            location.hash === "#"
        ) {

            history.replaceState(
                null,
                "",
                "#activitySection"
            );
        }

    }


    /* =====================================================
       ADMIN
    ===================================================== */

    else {

        /* -----------------------------------------------
           SHOW DASHBOARD NAV
        ------------------------------------------------ */

        if (dashboardNav) {

            dashboardNav.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW ACTIVITY NAV
        ------------------------------------------------ */

        if (activityNav) {

            activityNav.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW HISTORY NAV
        ------------------------------------------------ */

        if (recordsNav) {

            recordsNav.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW STAFF ACCOUNTS NAV
        ------------------------------------------------ */

        if (usersNav) {

            usersNav.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW REPORT NAV
        ------------------------------------------------ */

        if (reportsNav) {

            reportsNav.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW HERO
        ------------------------------------------------ */

        if (hero) {

            hero.style.display =
                "flex";
        }


        /* -----------------------------------------------
           SHOW STATISTICS
        ------------------------------------------------ */

        if (stats) {

            stats.style.display =
                "grid";
        }


        /* -----------------------------------------------
           SHOW ACTIVITY HISTORY
        ------------------------------------------------ */

        if (recordsSection) {

            recordsSection.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW ADMIN STAFF ACCOUNTS
        ------------------------------------------------ */

        if (adminUsersSection) {

            adminUsersSection.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW ACTIVITY
        ------------------------------------------------ */

        if (activitySection) {

            activitySection.style.display =
                "block";
        }


        /* -----------------------------------------------
           SHOW REPORTS
        ------------------------------------------------ */

        if (reportsSection) {

            reportsSection.style.display =
                "block";
        }
    }
}


/* =========================================================
   SHOW LOGGED-IN DASHBOARD
========================================================= */

function setLoggedIn() {

    pendingAdmin = null;


    $("loginPage")
        ?.classList.add(
            "hidden"
        );


    $("otpPage")
        ?.classList.add(
            "hidden"
        );


    $("app")
        ?.classList.remove(
            "hidden"
        );


    /* =====================================================
       USER NAME
    ===================================================== */

    if ($("userName")) {

        $("userName").textContent =
            currentUser?.name || "";
    }


    /* =====================================================
       ROLE
    ===================================================== */

    if ($("roleText")) {

        $("roleText").textContent =
            currentUser?.role || "";
    }


    /* =====================================================
       APPLY ROLE UI
    ===================================================== */

    applyRoleBasedUI();


    /* =====================================================
       LOAD ACTIVITIES
    ===================================================== */

    loadActivities();


    /* =====================================================
       ADMIN USERS
    ===================================================== */

    if (
        currentUser?.role === "ADMIN"
    ) {

        loadUsers();
    }
}


/* =========================================================
   LOGOUT
========================================================= */

function logout() {

    localStorage.removeItem(
        "bidyatra_token"
    );


    localStorage.removeItem(
        "bidyatra_user"
    );


    token = null;

    currentUser = null;

    pendingAdmin = null;

    activities = [];


    $("app")
        ?.classList.add(
            "hidden"
        );


    $("otpPage")
        ?.classList.add(
            "hidden"
        );


    $("loginPage")
        ?.classList.remove(
            "hidden"
        );


    if ($("username")) {

        $("username").value = "";
    }


    if ($("password")) {

        $("password").value = "";
    }


    if ($("otp")) {

        $("otp").value = "";
    }


    if ($("loginError")) {

        $("loginError").textContent =
            "";
    }


    if ($("otpError")) {

        $("otpError").textContent =
            "";
    }
}


/* =========================================================
   LOGIN
========================================================= */

if ($("loginForm")) {

    $("loginForm").onsubmit =
        async e => {

            e.preventDefault();


            if ($("loginError")) {

                $("loginError").textContent =
                    "";
            }


            const username =
                $("username")
                    .value
                    .trim();


            const password =
                $("password")
                    .value;


            if (!username || !password) {

                $("loginError").textContent =
                    "Please enter username and password.";

                return;
            }


            try {

                console.log(
                    "LOGIN REQUEST:",
                    username
                );


                const data =
                    await api(
                        "/auth/login",
                        {
                            method: "POST",

                            body:
                                JSON.stringify({

                                    username:
                                        username,

                                    password:
                                        password
                                })
                        }
                    );


                console.log(
                    "LOGIN RESPONSE:",
                    data
                );


                /* =================================================
                   ADMIN → OTP
                ================================================= */

                if (
                    data &&
                    data.requiresOtp === true
                ) {

                    pendingAdmin =
                        String(
                            data.username ||
                            username
                        ).trim();


                    console.log(
                        "OTP SENT FOR ADMIN:",
                        pendingAdmin
                    );


                    if (!pendingAdmin) {

                        $("loginError").textContent =
                            "Admin username missing from server response.";

                        return;
                    }


                    $("loginPage")
                        ?.classList.add(
                            "hidden"
                        );


                    $("otpPage")
                        ?.classList.remove(
                            "hidden"
                        );


                    if ($("otp")) {

                        $("otp").value = "";

                        $("otp").focus();
                    }


                    if ($("otpError")) {

                        $("otpError").textContent =
                            "Verification code sent to your email.";
                    }


                    return;
                }


                /* =================================================
                   STAFF → DIRECT LOGIN
                ================================================= */

                finishLogin(data);
            }


            catch (error) {

                console.error(
                    "LOGIN ERROR:",
                    error
                );


                if ($("loginError")) {

                    $("loginError").textContent =
                        error.message ===
                        "Unauthorized"

                            ? "Invalid username or password."

                            : (
                                error.message ||
                                "Login failed."
                            );
                }
            }
        };
}


/* =========================================================
   OTP VERIFICATION
========================================================= */

if ($("otpForm")) {

    $("otpForm").onsubmit =
        async e => {

            e.preventDefault();


            if ($("otpError")) {

                $("otpError").textContent =
                    "";
            }


            const username =
                String(
                    pendingAdmin || ""
                ).trim();


            const code =
                String(
                    $("otp")?.value || ""
                ).trim();


            console.log(
                "================================="
            );


            console.log(
                "VERIFY OTP REQUEST"
            );


            console.log(
                "Username:",
                username
            );


            console.log(
                "OTP:",
                code
            );


            console.log(
                "OTP Length:",
                code.length
            );


            console.log(
                "================================="
            );


            if (!username) {

                $("otpError").textContent =
                    "Admin session expired. Please login again.";

                return;
            }


            if (!/^\d{6}$/.test(code)) {

                $("otpError").textContent =
                    "Please enter a valid 6-digit OTP.";

                $("otp")?.focus();

                return;
            }


            try {

                const data =
                    await api(
                        "/auth/verify-otp",
                        {
                            method: "POST",

                            body:
                                JSON.stringify({

                                    username:
                                        username,

                                    code:
                                        code
                                })
                        }
                    );


                console.log(
                    "OTP VERIFIED SUCCESSFULLY"
                );


                console.log(
                    "VERIFY RESPONSE:",
                    data
                );


                finishLogin(data);
            }


            catch (error) {

                console.error(
                    "OTP VERIFY ERROR:",
                    error
                );


                if ($("otpError")) {

                    $("otpError").textContent =
                        error.message ||
                        "Invalid or expired code.";
                }
            }
        };
}


/* =========================================================
   FINISH LOGIN
========================================================= */

function finishLogin(data) {

    if (
        !data ||
        !data.token
    ) {

        throw new Error(
            "Login successful but authentication token was not received."
        );
    }


    token =
        data.token;


    currentUser = {

        name:
            data.name,

        username:
            data.username,

        role:
            data.role
    };


    localStorage.setItem(
        "bidyatra_token",
        token
    );


    localStorage.setItem(
        "bidyatra_user",
        JSON.stringify(
            currentUser
        )
    );


    pendingAdmin = null;


    if ($("otp")) {

        $("otp").value = "";
    }


    if ($("otpError")) {

        $("otpError").textContent =
            "";
    }


    setLoggedIn();
}


/* =========================================================
   LOGOUT BUTTON
========================================================= */

if ($("logoutBtn")) {

    $("logoutBtn").onclick =
        logout;
}


/* =========================================================
   ACTIVITY DATE
========================================================= */

if ($("date")) {

    $("date").value =
        today();
}


/* =========================================================
   ADD / UPDATE ACTIVITY
========================================================= */

if ($("activityForm")) {

    $("activityForm").onsubmit =
        async e => {

            e.preventDefault();


            const id =
                $("recordId").value;


            const body = {

                date:
                    $("date").value,

                staffName:
                    $("staffName")
                        .value
                        .trim(),

                visitingLocation:
                    $("location")
                        .value
                        .trim(),

                meetDriver:
                    Number(
                        $("drivers").value ||
                        0
                    ),

                login:
                    Number(
                        $("logins").value ||
                        0
                    ),

                documentsIssues:
                    Number(
                        $("docs").value ||
                        0
                    ),

                other:
                    $("remarks")
                        .value
                        .trim()
            };


            try {

                await api(

                    id
                        ? "/activities/" + id
                        : "/activities",

                    {

                        method:
                            id
                                ? "PUT"
                                : "POST",

                        body:
                            JSON.stringify(
                                body
                            )
                    }
                );


                toast(
                    id
                        ? "Record updated"
                        : "Daily record saved"
                );


                resetForm();

                loadActivities();
            }


            catch (error) {

                toast(
                    error.message ||
                    "Failed to save record."
                );
            }
        };
}


/* =========================================================
   RESET ACTIVITY FORM
========================================================= */

if ($("resetBtn")) {

    $("resetBtn").onclick =
        resetForm;
}


function resetForm() {

    if (!$("activityForm")) {
        return;
    }


    $("recordId").value = "";


    $("formTitle").textContent =
        "Add Daily Activity";


    $("saveBtn").textContent =
        "Save Daily Record";


    $("activityForm").reset();


    $("date").value =
        today();


    $("drivers").value =
        0;


    $("logins").value =
        0;


    $("docs").value =
        0;
}


/* =========================================================
   LOAD ACTIVITIES
========================================================= */

async function loadActivities() {

    if (!token) {
        return;
    }


    try {

        const date =
            $("filterDate")?.value || "";


        activities =
            await api(
                "/activities" +
                (
                    date
                        ? "?date=" +
                          encodeURIComponent(
                              date
                          )
                        : ""
                )
            );


        render();

        stats();
    }


    catch (error) {

        toast(
            error.message ||
            "Unable to load activities."
        );
    }
}


/* =========================================================
   RENDER ACTIVITIES
========================================================= */

function render() {

    if (!$("records")) {
        return;
    }


    const query =
        (
            $("search")?.value ||
            ""
        )
            .toLowerCase()
            .trim();


    const list =
        activities.filter(
            activity => {

                const staffName =
                    (
                        activity.staffName ||
                        ""
                    ).toLowerCase();


                const location =
                    (
                        activity.visitingLocation ||
                        ""
                    ).toLowerCase();


                return (
                    staffName.includes(
                        query
                    ) ||
                    location.includes(
                        query
                    )
                );
            }
        );


    $("records").innerHTML =
        "";


    if ($("empty")) {

        $("empty").style.display =
            list.length
                ? "none"
                : "block";
    }


    list.forEach(
        activity => {

            const tr =
                document.createElement(
                    "tr"
                );


            tr.innerHTML = `

                <td>
                    ${escapeHtml(
                        activity.date
                    )}
                </td>

                <td>
                    <b>
                        ${escapeHtml(
                            activity.staffName
                        )}
                    </b>
                </td>

                <td>
                    ${escapeHtml(
                        activity.visitingLocation ||
                        "-"
                    )}
                </td>

                <td>
                    ${Number(
                        activity.meetDriver ||
                        0
                    )}
                </td>

                <td>
                    ${Number(
                        activity.login ||
                        0
                    )}
                </td>

                <td>
                    ${Number(
                        activity.documentsIssues ||
                        0
                    )}
                </td>

                <td>
                    ${escapeHtml(
                        activity.other ||
                        "-"
                    )}
                </td>

                <td>

                    <button
                        class="action-btn edit"
                        onclick="editActivity('${activity.id}')"
                    >
                        Edit
                    </button>

                    <button
                        class="action-btn delete"
                        onclick="deleteActivity('${activity.id}')"
                    >
                        Delete
                    </button>

                </td>
            `;


            $("records")
                .appendChild(tr);
        }
    );
}


/* =========================================================
   EDIT ACTIVITY
========================================================= */

window.editActivity =
    id => {

        const activity =
            activities.find(
                x => x.id === id
            );


        if (!activity) {
            return;
        }


        $("recordId").value =
            activity.id;


        $("formTitle").textContent =
            "Edit Daily Activity";


        $("saveBtn").textContent =
            "Update Record";


        $("date").value =
            activity.date;


        $("staffName").value =
            activity.staffName ||
            "";


        $("location").value =
            activity.visitingLocation ||
            "";


        $("drivers").value =
            activity.meetDriver ||
            0;


        $("logins").value =
            activity.login ||
            0;


        $("docs").value =
            activity.documentsIssues ||
            0;


        $("remarks").value =
            activity.other ||
            "";


        location.hash =
            "#activitySection";
    };


/* =========================================================
   DELETE ACTIVITY
========================================================= */

window.deleteActivity =
    async id => {

        if (
            !confirm(
                "Delete this record?"
            )
        ) {

            return;
        }


        try {

            await api(
                "/activities/" + id,
                {
                    method: "DELETE"
                }
            );


            toast(
                "Record deleted"
            );


            loadActivities();
        }


        catch (error) {

            toast(
                error.message ||
                "Delete failed."
            );
        }
    };


/* =========================================================
   STATISTICS
========================================================= */

function stats() {

    if (!currentUser) {
        return;
    }


    const todayActivities =
        activities.filter(
            activity =>
                activity.date ===
                today()
        );


    if ($("statStaff")) {

        $("statStaff").textContent =

            currentUser.role === "ADMIN"

                ? new Set(
                    todayActivities.map(
                        activity =>
                            activity.createdBy
                    )
                ).size

                : 1;
    }


    if ($("statDrivers")) {

        $("statDrivers").textContent =
            todayActivities.reduce(
                (
                    sum,
                    activity
                ) =>
                    sum +
                    Number(
                        activity.meetDriver ||
                        0
                    ),
                0
            );
    }


    if ($("statLogins")) {

        $("statLogins").textContent =
            todayActivities.reduce(
                (
                    sum,
                    activity
                ) =>
                    sum +
                    Number(
                        activity.login ||
                        0
                    ),
                0
            );
    }


    if ($("statDocs")) {

        $("statDocs").textContent =
            todayActivities.reduce(
                (
                    sum,
                    activity
                ) =>
                    sum +
                    Number(
                        activity.documentsIssues ||
                        0
                    ),
                0
            );
    }
}


/* =========================================================
   SEARCH / FILTER
========================================================= */

if ($("search")) {

    $("search").oninput =
        render;
}


if ($("filterDate")) {

    $("filterDate").onchange =
        loadActivities;
}


if ($("clearFilter")) {

    $("clearFilter").onclick =
        () => {

            $("filterDate").value =
                "";

            loadActivities();
        };
}


/* =========================================================
   REPORT BUTTONS
========================================================= */

if ($("excelBtn")) {

    $("excelBtn").onclick =
        () => report("excel");
}


if ($("pdfBtn")) {

    $("pdfBtn").onclick =
        () => report("pdf");
}


/* =========================================================
   REPORT DOWNLOAD
========================================================= */

async function report(type) {

    try {

        const date =
            $("filterDate")?.value ||
            "";


        const response =
            await fetch(
                API +
                "/reports/" +
                type +
                (
                    date
                        ? "?date=" +
                          encodeURIComponent(
                              date
                          )
                        : ""
                ),

                {
                    headers: {

                        Authorization:
                            "Bearer " +
                            token
                    }
                }
            );


        if (!response.ok) {

            let message =
                "Report download failed.";


            try {

                const text =
                    await response.text();


                if (text) {

                    try {

                        const json =
                            JSON.parse(
                                text
                            );


                        message =
                            json.message ||
                            json.error ||
                            message;

                    }

                    catch {

                        message =
                            text;
                    }
                }

            }

            catch {
                // Ignore
            }


            throw new Error(
                message
            );
        }


        const blob =
            await response.blob();


        const url =
            URL.createObjectURL(
                blob
            );


        const link =
            document.createElement(
                "a"
            );


        link.href =
            url;


        link.download =
            type === "excel"

                ? "bidyatra-my-report.xlsx"

                : "bidyatra-my-report.pdf";


        document.body.appendChild(
            link
        );


        link.click();


        link.remove();


        setTimeout(
            () => {

                URL.revokeObjectURL(
                    url
                );

            },
            1000
        );
    }


    catch (error) {

        toast(
            error.message ||
            "Report download failed."
        );
    }
}


/* =========================================================
   ADMIN - CREATE USER
========================================================= */

if ($("userForm")) {

    $("userForm").onsubmit =
        async e => {

            e.preventDefault();


            /* Staff should never create users */

            if (
                currentUser?.role !== "ADMIN"
            ) {

                toast(
                    "Access denied."
                );

                return;
            }


            try {

                await api(
                    "/admin/users",
                    {

                        method: "POST",

                        body:
                            JSON.stringify({

                                name:
                                    $("newName")
                                        .value
                                        .trim(),

                                username:
                                    $("newUsername")
                                        .value
                                        .trim(),

                                password:
                                    $("newPassword")
                                        .value,

                                role:
                                    $("newRole")
                                        .value
                            })
                    }
                );


                toast(
                    "Account created"
                );


                e.target.reset();

                loadUsers();
            }


            catch (error) {

                toast(
                    error.message ||
                    "Account creation failed."
                );
            }
        };
}


/* =========================================================
   ADMIN - LOAD USERS
========================================================= */

async function loadUsers() {

    if (
        currentUser?.role !==
        "ADMIN"
    ) {

        return;
    }


    try {

        const users =
            await api(
                "/admin/users"
            );


        if (!$("users")) {
            return;
        }


        $("users").innerHTML =
            users.map(
                user => `

                    <tr>

                        <td>
                            ${escapeHtml(
                                user.name
                            )}
                        </td>

                        <td>
                            ${escapeHtml(
                                user.username
                            )}
                        </td>

                        <td>
                            ${escapeHtml(
                                user.role
                            )}
                        </td>

                        <td>
                            ${
                                user.enabled
                                    ? "Active"
                                    : "Disabled"
                            }
                        </td>

                        <td>

                            <button
                                class="action-btn delete"
                                onclick="deleteUser('${user.id}')"
                            >
                                Delete
                            </button>

                        </td>

                    </tr>
                `
            )
            .join("");
    }


    catch (error) {

        toast(
            error.message ||
            "Unable to load users."
        );
    }
}


/* =========================================================
   ADMIN - DELETE USER
========================================================= */

window.deleteUser =
    async id => {

        if (
            currentUser?.role !==
            "ADMIN"
        ) {

            toast(
                "Access denied."
            );

            return;
        }


        if (
            !confirm(
                "Delete this account?"
            )
        ) {

            return;
        }


        try {

            await api(
                "/admin/users/" + id,
                {
                    method: "DELETE"
                }
            );


            loadUsers();


            toast(
                "Account deleted"
            );
        }


        catch (error) {

            toast(
                error.message ||
                "Account deletion failed."
            );
        }
    };


/* =========================================================
   ESCAPE HTML
========================================================= */

function escapeHtml(value) {

    return String(
        value ?? ""
    )
        .replaceAll(
            "&",
            "&amp;"
        )
        .replaceAll(
            "<",
            "&lt;"
        )
        .replaceAll(
            ">",
            "&gt;"
        )
        .replaceAll(
            '"',
            "&quot;"
        )
        .replaceAll(
            "'",
            "&#039;"
        );
}


/* =========================================================
   OTP INPUT
========================================================= */

if ($("otp")) {

    $("otp").setAttribute(
        "maxlength",
        "6"
    );


    $("otp").setAttribute(
        "inputmode",
        "numeric"
    );


    $("otp").setAttribute(
        "autocomplete",
        "one-time-code"
    );


    $("otp").addEventListener(
        "input",
        () => {

            $("otp").value =
                $("otp")
                    .value
                    .replace(/\D/g, "")
                    .slice(0, 6);
        }
    );
}


/* =========================================================
   NAVIGATION CONTROL
========================================================= */

document.addEventListener(
    "click",
    event => {

        const link =
            event.target.closest(
                ".sidebar nav a"
            );


        if (!link || !currentUser) {
            return;
        }


        const isAdmin =
            currentUser.role ===
            "ADMIN";


        /* Staff can ONLY access these */

        if (!isAdmin) {

            const allowed =
                [
                    "#activitySection",
                    "#reportsSection"
                ];


            if (
                !allowed.includes(
                    link.getAttribute("href")
                )
            ) {

                event.preventDefault();


                location.hash =
                    "#activitySection";

                return;
            }
        }
    }
);


/* =========================================================
   HASH CHANGE PROTECTION
========================================================= */

window.addEventListener(
    "hashchange",
    () => {

        if (!currentUser) {
            return;
        }


        if (
            currentUser.role !==
            "ADMIN"
        ) {

            const allowed =
                [
                    "#activitySection",
                    "#reportsSection"
                ];


            if (
                !allowed.includes(
                    location.hash
                )
            ) {

                history.replaceState(
                    null,
                    "",
                    "#activitySection"
                );
            }
        }
    }
);


/* =========================================================
   INITIAL APPLICATION LOAD
========================================================= */

if (
    token &&
    currentUser
) {

    setLoggedIn();
}