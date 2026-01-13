import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegleConfigurationComponent } from './regle-configuration.component';

describe('RegleConfigurationComponent', () => {
  let component: RegleConfigurationComponent;
  let fixture: ComponentFixture<RegleConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegleConfigurationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegleConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
