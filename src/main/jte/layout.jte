@import gg.jte.Content
@param String title
@param String username
@param Content pageContent
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title} — Cinnamon</title>
    <link rel="icon" type="image/svg+xml" href="/static/ui/favicon.svg">
    <link rel="stylesheet" href="/static/ui/bulma.min.css">
    <script src="/static/ui/htmx.min.js"></script>
    <script src="/static/ui/cinnamon-ui.js"></script>
</head>
<body>
<nav class="navbar is-dark" role="navigation" aria-label="main navigation">
    <div class="navbar-brand">
        <a class="navbar-item has-text-white has-text-weight-bold" href="/ui/folders">Cinnamon</a>
    </div>
    <div class="navbar-menu">
        <div class="navbar-start">
            <a class="navbar-item" href="/ui/folders">Home</a>
        </div>
        <div class="navbar-end">
            <div class="navbar-item">
                <span class="has-text-grey-light is-size-7">${username}</span>
            </div>
            <a class="navbar-item" href="/ui/logout">Logout</a>
        </div>
    </div>
</nav>
${pageContent}
</body>
</html>
