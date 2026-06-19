@param String error
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login — Cinnamon</title>
    <link rel="stylesheet" href="/static/ui/bulma.min.css">
</head>
<body>
<section class="hero is-fullheight is-dark">
    <div class="hero-body">
        <div class="container">
            <div class="columns is-centered">
                <div class="column is-4-desktop is-6-tablet">
                    <div class="box">
                        <h1 class="title">Cinnamon</h1>
                        <h2 class="subtitle">Please log in</h2>
                        @if(!error.isEmpty())
                        <div class="notification is-danger is-light">
                            ${error}
                        </div>
                        @endif
                        <form method="POST" action="/ui/login">
                            <div class="field">
                                <label class="label">Username</label>
                                <div class="control">
                                    <input class="input" type="text" name="username" autocomplete="username" autofocus required>
                                </div>
                            </div>
                            <div class="field">
                                <label class="label">Password</label>
                                <div class="control">
                                    <input class="input" type="password" name="password" autocomplete="current-password" required>
                                </div>
                            </div>
                            <div class="field">
                                <div class="control">
                                    <button class="button is-primary is-fullwidth" type="submit">Login</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>
</body>
</html>
