import { Component } from '@angular/core';
import {ProfileEditComponent} from "../../../shared/compoments/edit-profil/edit-profil.component";

@Component({
  selector: 'app-stats',
    imports: [
        ProfileEditComponent
    ],
  templateUrl: './stats.component.html',
  styleUrl: './stats.component.css'
})
export class StatsComponent {

}
