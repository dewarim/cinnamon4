@import com.dewarim.cinnamon.model.Folder
@import java.util.List
@param Folder homeFolder
@param String homePath
@param List<Folder> subFolders
@param String currentFolderPath
<aside class="menu p-2">
    <p class="menu-label">Folders</p>
    <ul class="menu-list">
        <li>
            @if(currentFolderPath.equals(homePath))
                <a href="/ui/folders?folderPath=${homePath}" class="is-active">🏠 ${homeFolder.getName()}</a>
            @else
                <a href="/ui/folders?folderPath=${homePath}">🏠 ${homeFolder.getName()}</a>
            @endif
            @if(!subFolders.isEmpty())
            <ul>
                @for(Folder folder : subFolders)
                <li>
                    @if(currentFolderPath.equals(homePath + "/" + folder.getName()))
                        <a href="/ui/folders?folderPath=${homePath}/${folder.getName()}" class="is-active">📁 ${folder.getName()}</a>
                    @else
                        <a href="/ui/folders?folderPath=${homePath}/${folder.getName()}">📁 ${folder.getName()}</a>
                    @endif
                </li>
                @endfor
            </ul>
            @endif
        </li>
    </ul>
</aside>
